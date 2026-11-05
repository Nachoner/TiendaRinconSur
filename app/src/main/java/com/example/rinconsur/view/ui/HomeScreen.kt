package com.example.rinconsur.view.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rinconsur.view.utils.formatearFechaHora
import com.example.rinconsur.viewmodels.DashboardViewModel
import com.example.rinconsur.viewmodels.RegistroViewModel
import com.example.rinconsur.viewmodels.TurnoViewModel
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    nombre: String,
    tipo: String,
    esOperador: Boolean,
    esSupervisor: Boolean,
    turnoViewModel: TurnoViewModel,
    registroViewModel: RegistroViewModel,
    dashboardViewModel: DashboardViewModel,
    onIrTurno: () -> Unit,
    onIrSalidas: () -> Unit,
    onIrMetricas: () -> Unit,
    onIrUsuarios: () -> Unit,
    onIrTipos: () -> Unit
) {
    // Animación de entrada del panel
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 6 }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Saludo(nombre = nombre, tipo = tipo)

                if (esOperador) {
                    PanelOperador(
                        turnoViewModel = turnoViewModel,
                        registroViewModel = registroViewModel,
                        onIrTurno = onIrTurno,
                        onIrSalidas = onIrSalidas
                    )
                }

                if (esSupervisor) {
                    PanelSupervisor(
                        dashboardViewModel = dashboardViewModel,
                        onIrMetricas = onIrMetricas,
                        onIrUsuarios = onIrUsuarios,
                        onIrTipos = onIrTipos
                    )
                }

                if (!esOperador && !esSupervisor) {
                    Text(
                        text = "Tu perfil todavía no tiene pantallas asignadas. " +
                                "Pide a un supervisor que te asigne un tipo de usuario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun Saludo(nombre: String, tipo: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Hola, $nombre", style = MaterialTheme.typography.titleLarge)
                Text("Perfil: $tipo", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PanelOperador(
    turnoViewModel: TurnoViewModel,
    registroViewModel: RegistroViewModel,
    onIrTurno: () -> Unit,
    onIrSalidas: () -> Unit
) {
    val turno by turnoViewModel.turnoActivo.collectAsStateWithLifecycle()
    val registros by registroViewModel.registros.collectAsStateWithLifecycle()
    val turnoActual = turno

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (turnoActual != null) "Turno en curso" else "Sin turno",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (turnoActual != null) {
                Text(
                    text = "Acceso ${turnoActual.accesoId} · desde ${formatearFechaHora(turnoActual.inicio)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // El número cambia con animación cada vez que se registra una salida
                    AnimatedContent(targetState = registros.size, label = "salidas") { n ->
                        Text(
                            text = "$n",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("salidas registradas", style = MaterialTheme.typography.bodyMedium)
                }
                Button(
                    onClick = onIrSalidas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar salida")
                }
            } else {
                Text(
                    text = "Inicia un turno para empezar a registrar salidas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onIrTurno,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciar turno")
                }
            }
        }
    }

    Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Atajo(
            icono = Icons.Default.AccessTime,
            titulo = "Turno",
            detalle = "Iniciar o finalizar",
            onClick = onIrTurno,
            modifier = Modifier.weight(1f)
        )
        Atajo(
            icono = Icons.AutoMirrored.Filled.ExitToApp,
            titulo = "Salidas",
            detalle = "Ver y editar",
            onClick = onIrSalidas,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PanelSupervisor(
    dashboardViewModel: DashboardViewModel,
    onIrMetricas: () -> Unit,
    onIrUsuarios: () -> Unit,
    onIrTipos: () -> Unit
) {
    val accesos by dashboardViewModel.accesos.collectAsStateWithLifecycle()
    val periodo by dashboardViewModel.periodo.collectAsStateWithLifecycle()

    val totalGrupos = accesos.sumOf { it.grupos }
    val conBolsa = accesos.sumOf { it.gruposConBolsa }
    val personas = accesos.sumOf { it.personas }
    val porcentaje = if (totalGrupos == 0) 0 else (conBolsa * 100f / totalGrupos).roundToInt()
    val etiqueta = dashboardViewModel.periodos[periodo].etiqueta

    // Resumen de las métricas. Al tocarlo se abre el dashboard completo
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onIrMetricas)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Resumen · $etiqueta",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
            if (totalGrupos == 0) {
                Text(
                    text = "Aún no hay salidas registradas en este período.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DatoResumen(valor = totalGrupos, etiqueta = "Grupos")
                    DatoResumen(valor = personas, etiqueta = "Personas")
                    DatoResumen(valor = porcentaje, etiqueta = "Con bolsa", sufijo = "%")
                }
            }
        }
    }

    Text("Gestión", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Atajo(
            icono = Icons.Default.People,
            titulo = "Usuarios",
            detalle = "Crear y editar",
            onClick = onIrUsuarios,
            modifier = Modifier.weight(1f)
        )
        Atajo(
            icono = Icons.Default.Badge,
            titulo = "Tipos de usuario",
            detalle = "Administrar",
            onClick = onIrTipos,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DatoResumen(valor: Int, etiqueta: String, sufijo: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(targetState = valor, label = "dato") { v ->
            Text(
                text = "$v$sufijo",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(text = etiqueta, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun Atajo(
    icono: ImageVector,
    titulo: String,
    detalle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}