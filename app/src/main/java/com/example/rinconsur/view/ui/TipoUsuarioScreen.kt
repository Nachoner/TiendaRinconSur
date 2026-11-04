package com.example.rinconsur.view.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rinconsur.repository.model.TipoUsuario
import com.example.rinconsur.repository.model.TiposBase
import com.example.rinconsur.view.utils.CampoFormulario
import com.example.rinconsur.view.utils.vibrarConfirmacion
import com.example.rinconsur.view.utils.vibrarError
import com.example.rinconsur.viewmodels.TipoUsuarioViewModel

@Composable
fun TipoUsuarioScreen(viewModel: TipoUsuarioViewModel) {
    val context = LocalContext.current
    val tipos by viewModel.tipos.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val mensaje by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var dialogoAbierto by rememberSaveable { mutableStateOf(false) }
    var porEliminar by remember { mutableStateOf<TipoUsuario?>(null) }

    // Los mensajes generales (por ejemplo, un tipo en uso) salen como aviso inferior
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // El teléfono vibra dos veces si el campo del diálogo tiene error
    LaunchedEffect(form.nombreError) {
        if (form.nombreError != null && dialogoAbierto) vibrarError(context)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.nuevo()
                    dialogoAbierto = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo tipo") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tipos, key = { it.id }) { tipo ->
                TipoItem(
                    tipo = tipo,
                    onEditar = {
                        viewModel.editar(tipo)
                        dialogoAbierto = true
                    },
                    onEliminar = { porEliminar = tipo },
                    // Animación al agregar, quitar o reordenar elementos
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    // Formulario de crear o editar
    if (dialogoAbierto) {
        AlertDialog(
            onDismissRequest = { dialogoAbierto = false },
            title = {
                Text(if (form.id == 0L) "Nuevo tipo de usuario" else "Editar tipo de usuario")
            },
            text = {
                CampoFormulario(
                    valor = form.nombre,
                    onCambio = viewModel::onNombreChange,
                    etiqueta = "Nombre del tipo",
                    icono = Icons.Default.Badge,
                    error = form.nombreError,
                    imeAction = ImeAction.Done,
                    onDone = {
                        viewModel.guardar {
                            vibrarConfirmacion(context)
                            dialogoAbierto = false
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.guardar {
                        vibrarConfirmacion(context)
                        dialogoAbierto = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogoAbierto = false }) { Text("Cancelar") }
            }
        )
    }

    // Confirmación antes de eliminar
    porEliminar?.let { tipo ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("¿Eliminar tipo de usuario?") },
            text = { Text("Se eliminará \"${tipo.nombre}\". Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(tipo)
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
private fun TipoItem(
    tipo: TipoUsuario,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esBase = TiposBase.esBase(tipo.id)

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Badge,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tipo.nombre, style = MaterialTheme.typography.titleMedium)
                if (esBase) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Tipo base, no se puede modificar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            IconButton(onClick = onEditar, enabled = !esBase) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onEliminar, enabled = !esBase) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}