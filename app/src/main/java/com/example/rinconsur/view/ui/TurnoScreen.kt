package com.example.rinconsur.view.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rinconsur.repository.model.Turno
import com.example.rinconsur.view.utils.FotoMiniatura
import com.example.rinconsur.view.utils.MensajeError
import com.example.rinconsur.view.utils.formatearFechaHora
import com.example.rinconsur.view.utils.vibrarConfirmacion
import com.example.rinconsur.view.utils.vibrarError
import com.example.rinconsur.viewmodels.TurnoFormState
import com.example.rinconsur.viewmodels.TurnoViewModel
import java.io.File

@Composable
fun TurnoScreen(viewModel: TurnoViewModel) {
    val context = LocalContext.current
    val form by viewModel.form.collectAsStateWithLifecycle()
    val turnoActivo by viewModel.turnoActivo.collectAsStateWithLifecycle()

    var camaraAbierta by rememberSaveable { mutableStateOf(false) }
    var permisoDenegado by rememberSaveable { mutableStateOf(false) }

    // Solicitud del permiso de cámara en tiempo de ejecución
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { otorgado ->
        permisoDenegado = !otorgado
        camaraAbierta = otorgado
    }

    fun tomarFoto() {
        val concedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (concedido) {
            permisoDenegado = false
            camaraAbierta = true
        } else {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // El teléfono vibra dos veces cuando el formulario tiene errores
    LaunchedEffect(form.accesoError, form.fotoError) {
        if (form.accesoError != null || form.fotoError != null) vibrarError(context)
    }

    if (camaraAbierta) {
        CamaraPuerta(
            onFotoLista = { ruta ->
                // Si se repite la foto, se borra la anterior
                viewModel.form.value.fotoRuta?.let { File(it).delete() }
                viewModel.onFotoChange(ruta)
                vibrarConfirmacion(context)
                camaraAbierta = false
            },
            onCancelar = { camaraAbierta = false }
        )
    } else {
        Crossfade(targetState = turnoActivo, label = "turno") { turno ->
            if (turno != null) {
                TurnoEnCurso(turno = turno, viewModel = viewModel)
            } else {
                FormularioInicio(
                    form = form,
                    viewModel = viewModel,
                    permisoDenegado = permisoDenegado,
                    onTomarFoto = { tomarFoto() },
                    onAbrirAjustes = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun FormularioInicio(
    form: TurnoFormState,
    viewModel: TurnoViewModel,
    permisoDenegado: Boolean,
    onTomarFoto: () -> Unit,
    onAbrirAjustes: () -> Unit
) {
    val context = LocalContext.current
    val ruta = form.fotoRuta

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Iniciar turno", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Elige el acceso donde te ubicarás y toma una foto de la puerta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Acceso
        Spacer(Modifier.height(4.dp))
        Text("Acceso", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipAcceso(1, form.accesoId == 1, { viewModel.onAccesoChange(1) }, Modifier.weight(1f))
            ChipAcceso(2, form.accesoId == 2, { viewModel.onAccesoChange(2) }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipAcceso(3, form.accesoId == 3, { viewModel.onAccesoChange(3) }, Modifier.weight(1f))
            ChipAcceso(4, form.accesoId == 4, { viewModel.onAccesoChange(4) }, Modifier.weight(1f))
        }
        MensajeError(form.accesoError)

        // Foto
        Spacer(Modifier.height(4.dp))
        Text("Foto de la puerta", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "La cámara se usa solo para fotografiar la puerta o su señalética. No se capturan personas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // La miniatura aparece con animación cuando hay foto
        AnimatedVisibility(
            visible = ruta != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            if (ruta != null) {
                FotoMiniatura(
                    ruta = ruta,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        OutlinedButton(
            onClick = onTomarFoto,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (ruta == null) "Tomar foto de la puerta" else "Volver a tomar la foto")
        }
        MensajeError(form.fotoError)

        // Aviso si el usuario negó el permiso de cámara
        AnimatedVisibility(
            visible = permisoDenegado,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Sin permiso de cámara", style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        text = "Para iniciar el turno necesitas permitir el uso de la cámara. " +
                                "Puedes activarlo en los ajustes de la aplicación.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onAbrirAjustes) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Abrir ajustes")
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.iniciar { vibrarConfirmacion(context) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Iniciar turno")
        }
    }
}

@Composable
private fun ChipAcceso(
    numero: Int,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text("Acceso $numero", modifier = Modifier.padding(vertical = 8.dp)) },
        leadingIcon = {
            Icon(
                imageVector = if (seleccionado) Icons.Default.Check else Icons.Default.MeetingRoom,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        },
        modifier = modifier
    )
}

@Composable
private fun TurnoEnCurso(turno: Turno, viewModel: TurnoViewModel) {
    val context = LocalContext.current
    var confirmar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
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
                    Text("Turno en curso", style = MaterialTheme.typography.titleLarge)
                }
                Text("Acceso ${turno.accesoId}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Inicio: ${formatearFechaHora(turno.inicio)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text("Foto de la puerta", style = MaterialTheme.typography.titleMedium)
        FotoMiniatura(
            ruta = turno.fotoRuta,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { confirmar = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.Default.Stop, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Finalizar turno")
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("¿Finalizar turno?") },
            text = {
                Text("Se guardará la hora de término y ya no podrás agregar registros a este turno.")
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    viewModel.finalizar(turno) { vibrarConfirmacion(context) }
                }) { Text("Finalizar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmar = false }) { Text("Cancelar") }
            }
        )
    }
}