package com.example.rinconsur.view.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rinconsur.repository.model.Registro
import com.example.rinconsur.repository.model.tamanoGrupo
import com.example.rinconsur.view.utils.formatearFechaHora
import com.example.rinconsur.view.utils.vibrarConfirmacion
import com.example.rinconsur.viewmodels.RegistroViewModel
import com.example.rinconsur.viewmodels.TurnoViewModel

@Composable
fun RegistroListScreen(
    registroViewModel: RegistroViewModel,
    turnoViewModel: TurnoViewModel,
    onAgregar: () -> Unit,
    onEditar: (Long) -> Unit,
    onIrATurno: () -> Unit
) {
    val context = LocalContext.current
    val turno by turnoViewModel.turnoActivo.collectAsStateWithLifecycle()
    val registros by registroViewModel.registros.collectAsStateWithLifecycle()
    val mensaje by registroViewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<Registro?>(null) }

    // Los mensajes generales (por ejemplo, un permiso) salen como aviso inferior
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            registroViewModel.clearMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val turnoActual = turno
        if (turnoActual == null) {
            SinTurno(onIrATurno = onIrATurno, modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                ResumenTurno(accesoId = turnoActual.accesoId, registros = registros)

                // Botón justo debajo del acceso, para registrar salidas más rápido
                Button(
                    onClick = onAgregar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar salida")
                }
                Spacer(Modifier.height(12.dp))

                if (registros.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Aún no hay salidas en este turno",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Toca \"Registrar salida\" cada vez que una persona o un grupo salga por tu acceso.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(registros, key = { it.id }) { registro ->
                            RegistroItem(
                                registro = registro,
                                onEditar = { onEditar(registro.id) },
                                onEliminar = { porEliminar = registro },
                                // Animación al agregar, quitar o reordenar elementos
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    porEliminar?.let { registro ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("¿Eliminar registro?") },
            text = {
                Text(
                    "Se eliminará la salida de las ${formatearFechaHora(registro.fechaHora)}. " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    registroViewModel.eliminar(registro)
                    vibrarConfirmacion(context)
                    porEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SinTurno(onIrATurno: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No tienes un turno en curso",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Inicia un turno para registrar salidas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onIrATurno) {
            Text("Ir a Turno")
        }
    }
}

@Composable
private fun ResumenTurno(accesoId: Int, registros: List<Registro>) {
    val conBolsa = registros.count { it.compro }
    val sinBolsa = registros.count { !it.compro }
    val personas = registros.sumOf { it.tamanoGrupo }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Acceso $accesoId", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Estadistica(valor = conBolsa, etiqueta = "Con bolsa")
                Estadistica(valor = sinBolsa, etiqueta = "Sin bolsa")
                Estadistica(valor = personas, etiqueta = "Personas")
            }
        }
    }
}

@Composable
private fun Estadistica(valor: Int, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // El número cambia con animación cada vez que se registra una salida
        AnimatedContent(targetState = valor, label = "contador") { v ->
            Text(
                text = "$v",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(text = etiqueta, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RegistroItem(
    registro: Registro,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = formatearFechaHora(registro.fechaHora),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Rango: ${registro.rangoEtario}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Grupo de ${registro.tamanoGrupo} " +
                            "(${registro.acompanantesAdultos} adultos y ${registro.acompanantesNinos} niños acompañantes)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                EtiquetaBolsa(compro = registro.compro)
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
private fun EtiquetaBolsa(compro: Boolean) {
    Surface(
        color = if (compro) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (compro) Icons.Default.ShoppingBag else Icons.Default.RemoveShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (compro) "Con bolsa" else "Sin bolsa",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}