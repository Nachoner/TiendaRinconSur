package com.example.rinconsur.view.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rinconsur.repository.model.UsuarioConTipo
import com.example.rinconsur.viewmodels.UsuarioViewModel

@Composable
fun UsuarioListScreen(
    viewModel: UsuarioViewModel,
    usuarioActualId: Long?,
    onAgregar: () -> Unit,
    onEditar: (Long) -> Unit
) {
    val usuarios by viewModel.usuarios.collectAsStateWithLifecycle()
    val mensaje by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Los mensajes generales salen como aviso inferior
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAgregar,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Nuevo usuario") }
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
            items(usuarios, key = { it.usuario.id }) { item ->
                UsuarioItem(
                    item = item,
                    esActual = item.usuario.id == usuarioActualId,
                    onEditar = { onEditar(item.usuario.id) },
                    onCambiarActivo = { activo ->
                        viewModel.cambiarActivo(item.usuario, activo, usuarioActualId)
                    },
                    // Animación al agregar, quitar o reordenar elementos
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun UsuarioItem(
    item: UsuarioConTipo,
    esActual: Boolean,
    onEditar: () -> Unit,
    onCambiarActivo: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val usuario = item.usuario

    // Animación: la tarjeta cambia de color al activar o desactivar la cuenta
    val fondo by animateColorAsState(
        targetValue = if (usuario.activo) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "fondo"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = fondo),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = if (usuario.activo) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = usuario.nombre + if (esActual) " (tú)" else "",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.tipo.nombre} · ${if (usuario.activo) "Activa" else "Desactivada"}",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            // Nadie puede desactivar su propia cuenta
            Switch(
                checked = usuario.activo,
                onCheckedChange = onCambiarActivo,
                enabled = !esActual
            )
        }
    }
}