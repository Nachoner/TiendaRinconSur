package com.example.rinconsur.view.ui

import androidx.activity.compose.BackHandler
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

/**
 * Pantalla de cámara para fotografiar la puerta al iniciar el turno.
 * [onFotoLista] recibe la ruta del archivo guardado. [onCancelar] cierra la cámara sin foto.
 */
@Composable
fun CamaraPuerta(
    onFotoLista: (String) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val camara = remember { LifecycleCameraController(context) }
    var capturando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // La cámara se enlaza al ciclo de vida de la pantalla y se libera al salir
    DisposableEffect(lifecycleOwner) {
        camara.bindToLifecycle(lifecycleOwner)
        onDispose { camara.unbind() }
    }

    // El botón "atrás" del teléfono cierra la cámara
    BackHandler(onBack = onCancelar)

    fun tomarFoto() {
        val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
        val archivo = File(carpeta, "turno_${System.currentTimeMillis()}.jpg")
        val opciones = ImageCapture.OutputFileOptions.Builder(archivo).build()

        capturando = true
        error = null
        camara.takePicture(
            opciones,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturando = false
                    onFotoLista(archivo.absolutePath)
                }

                override fun onError(exception: ImageCaptureException) {
                    capturando = false
                    error = "No se pudo tomar la foto. Intenta de nuevo."
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply { this.controller = camara }
            },
            modifier = Modifier.fillMaxSize()
        )

        Surface(
            color = Color.Black.copy(alpha = 0.55f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Text(
                text = "Encuadra la puerta o su señalética. No incluyas personas.",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(12.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (error != null) {
                Surface(color = Color.Black.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small) {
                    Text(
                        text = error ?: "",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                FilledTonalIconButton(onClick = onCancelar) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar")
                }
                FilledIconButton(
                    onClick = { tomarFoto() },
                    enabled = !capturando,
                    modifier = Modifier.size(72.dp)
                ) {
                    if (capturando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Tomar foto",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                // Espacio para que el botón de captura quede centrado
                Spacer(Modifier.size(40.dp))
            }
        }
    }
}