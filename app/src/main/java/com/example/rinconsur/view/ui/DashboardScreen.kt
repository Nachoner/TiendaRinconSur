package com.example.rinconsur.view.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rinconsur.viewmodels.AccesoMetricas
import com.example.rinconsur.viewmodels.BarraDato
import com.example.rinconsur.viewmodels.DashboardViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val periodo by viewModel.periodo.collectAsStateWithLifecycle()
    val accesos by viewModel.accesos.collectAsStateWithLifecycle()
    val perfil by viewModel.perfilSinBolsa.collectAsStateWithLifecycle()
    val composicion by viewModel.composicionSinBolsa.collectAsStateWithLifecycle()
    val hayEjemplo by viewModel.hayEjemplo.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Los mensajes salen como aviso inferior
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val totalGrupos = accesos.sumOf { it.grupos }
    val gruposConBolsa = accesos.sumOf { it.gruposConBolsa }
    val totalPersonas = accesos.sumOf { it.personas }
    val porcentajeConBolsa =
        if (totalGrupos == 0) 0 else (gruposConBolsa * 100f / totalGrupos).roundToInt()
    // El acceso con más salidas sin bolsa en proporción
    val accesoMasTransito = accesos
        .filter { it.grupos > 0 }
        .maxByOrNull { 1f - it.fraccionConBolsa }
        ?.accesoId

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Métricas de flujo", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Salidas con bolsa (compra) frente a salidas sin bolsa (solo tránsito).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Período
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                viewModel.periodos.forEachIndexed { posicion, p ->
                    val seleccionado = periodo == posicion
                    FilterChip(
                        selected = seleccionado,
                        onClick = { viewModel.seleccionarPeriodo(posicion) },
                        label = { Text(p.etiqueta) },
                        leadingIcon = if (seleccionado) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )
                }
            }

            // Sin registros en el período
            AnimatedVisibility(
                visible = totalGrupos == 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No hay salidas registradas en este período",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Prueba con otro período o carga datos de ejemplo abajo.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Métricas
            AnimatedVisibility(
                visible = totalGrupos > 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Resumen general
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Indicador(valor = totalGrupos, etiqueta = "Grupos")
                            Indicador(valor = totalPersonas, etiqueta = "Personas")
                            Indicador(valor = porcentajeConBolsa, etiqueta = "Con bolsa", sufijo = "%")
                        }
                    }

                    // Por acceso
                    Titulo(icono = Icons.Default.MeetingRoom, texto = "Por acceso")
                    accesos.forEach { acceso ->
                        TarjetaAcceso(
                            metricas = acceso,
                            masTransito = acceso.accesoId == accesoMasTransito
                        )
                    }

                    // Perfil de quienes no compran
                    Titulo(icono = Icons.Default.People, texto = "Perfil de quienes salen sin bolsa")
                    Text(
                        text = "Rango etario de la persona principal de cada grupo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    GraficoBarras(
                        datos = perfil,
                        color = MaterialTheme.colorScheme.tertiary,
                        vacio = "No hay salidas sin bolsa en este período"
                    )

                    Titulo(icono = Icons.Default.Group, texto = "Cómo salen quienes no compran")
                    GraficoBarras(
                        datos = composicion,
                        color = MaterialTheme.colorScheme.secondary,
                        vacio = "No hay salidas sin bolsa en este período"
                    )
                }
            }

            // Datos de ejemplo
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Datos de ejemplo", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Son ficticios y se generan en este teléfono, para probar el dashboard sin " +
                                "datos reales. Borrarlos no afecta los registros reales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Supuesto: salir con bolsa de la tienda indica que la persona compró.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            if (hayEjemplo) viewModel.borrarEjemplo() else viewModel.cargarEjemplo()
                        },
                        enabled = !cargando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (hayEjemplo) Icons.Default.Delete else Icons.Default.Add,
                                contentDescription = null
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(if (hayEjemplo) "Borrar datos de ejemplo" else "Cargar datos de ejemplo")
                    }
                }
            }
        }
    }
}

@Composable
private fun Titulo(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun Indicador(valor: Int, etiqueta: String, sufijo: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // El número cambia con animación al cambiar de período
        AnimatedContent(targetState = valor, label = "indicador") { v ->
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
private fun TarjetaAcceso(metricas: AccesoMetricas, masTransito: Boolean) {
    // La barra crece con animación hasta su valor
    val fraccion by animateFloatAsState(
        targetValue = metricas.fraccionConBolsa,
        animationSpec = tween(800),
        label = "fraccion"
    )
    val porcentajeCompra = (metricas.fraccionConBolsa * 100).roundToInt()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Acceso ${metricas.accesoId}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (masTransito) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Mayor tránsito",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (metricas.grupos == 0) {
                Text(
                    text = "Sin registros en este período",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    text = "Con bolsa: ${metricas.gruposConBolsa} ($porcentajeCompra%)  ·  " +
                            "Sin bolsa: ${metricas.gruposSinBolsa} (${100 - porcentajeCompra}%)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${metricas.personas} personas en ${metricas.grupos} grupos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GraficoBarras(datos: List<BarraDato>, color: Color, vacio: String) {
    val total = datos.sumOf { it.cantidad }
    val maximo = datos.maxOfOrNull { it.cantidad } ?: 0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (total == 0) {
                Text(
                    text = vacio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                datos.forEach { dato ->
                    FilaBarra(
                        etiqueta = dato.etiqueta,
                        cantidad = dato.cantidad,
                        maximo = maximo,
                        total = total,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaBarra(etiqueta: String, cantidad: Int, maximo: Int, total: Int, color: Color) {
    val fraccion by animateFloatAsState(
        targetValue = if (maximo == 0) 0f else cantidad.toFloat() / maximo,
        animationSpec = tween(800),
        label = "barra"
    )
    val porcentaje = if (total == 0) 0 else (cantidad * 100f / total).roundToInt()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(100.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                    .background(color)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$cantidad ($porcentaje%)",
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.width(72.dp)
        )
    }
}