package com.example.rinconsur.view.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rinconsur.view.utils.CampoFormulario
import com.example.rinconsur.view.utils.vibrarConfirmacion
import com.example.rinconsur.view.utils.vibrarError
import com.example.rinconsur.viewmodels.TipoUsuarioViewModel
import com.example.rinconsur.viewmodels.UsuarioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsuarioFormScreen(
    viewModel: UsuarioViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    usuarioId: Long,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val form by viewModel.form.collectAsStateWithLifecycle()
    val tipos by tipoUsuarioViewModel.tipos.collectAsStateWithLifecycle()
    val editando = usuarioId != 0L

    // Al entrar, el formulario parte vacío (nuevo) o con los datos del usuario (editar)
    LaunchedEffect(usuarioId) {
        viewModel.nuevo()
        if (editando) viewModel.cargar(usuarioId)
    }

    // El teléfono vibra dos veces cuando hay errores en el formulario
    LaunchedEffect(form.nombreError, form.emailError, form.claveError) {
        if (form.nombreError != null || form.emailError != null || form.claveError != null) {
            vibrarError(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (editando) "Editar usuario" else "Nuevo usuario",
            style = MaterialTheme.typography.headlineSmall
        )

        CampoFormulario(
            valor = form.nombre,
            onCambio = viewModel::onNombreChange,
            etiqueta = "Nombre",
            icono = Icons.Default.Person,
            error = form.nombreError
        )
        CampoFormulario(
            valor = form.email,
            onCambio = viewModel::onEmailChange,
            etiqueta = "Email",
            icono = Icons.Default.Email,
            error = form.emailError,
            teclado = KeyboardType.Email
        )
        CampoFormulario(
            valor = form.clave,
            onCambio = viewModel::onClaveChange,
            etiqueta = if (editando) "Nueva contraseña (opcional)" else "Contraseña",
            icono = Icons.Default.Lock,
            error = form.claveError,
            esClave = true,
            imeAction = ImeAction.Done
        )

        AnimatedVisibility(
            visible = editando,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = "Deja la contraseña vacía para conservar la actual.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(4.dp))
        Text("Tipo de usuario", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tipos.forEach { tipo ->
                val seleccionado = form.tipoUsuarioId == tipo.id
                FilterChip(
                    selected = seleccionado,
                    onClick = { viewModel.onTipoChange(tipo.id) },
                    label = { Text(tipo.nombre) },
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

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.guardar {
                    vibrarConfirmacion(context)
                    onDone()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (editando) "Guardar cambios" else "Crear usuario")
        }
    }
}