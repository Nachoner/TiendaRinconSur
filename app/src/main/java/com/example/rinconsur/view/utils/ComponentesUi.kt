package com.example.rinconsur.view.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Mensaje de error en rojo con ícono. Aparece y desaparece con animación. */
@Composable
fun MensajeError(error: String?, modifier: Modifier = Modifier) {
    // Se recuerda el último mensaje para que no quede vacío mientras se anima la salida
    var ultimo by remember { mutableStateOf("") }
    val mostrado = error ?: ultimo
    SideEffect { if (error != null) ultimo = error }

    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = mostrado,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/** Muestra una foto guardada en el teléfono. Se carga en segundo plano con una corrutina. */
@Composable
fun FotoMiniatura(ruta: String, modifier: Modifier = Modifier) {
    val imagen by produceState<ImageBitmap?>(initialValue = null, key1 = ruta) {
        value = withContext(Dispatchers.IO) { cargarFoto(ruta) }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = imagen
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Foto de la puerta",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }
}

// Reduce la foto y corrige su orientación, porque la cámara la guarda con un giro en los metadatos
private fun cargarFoto(ruta: String): ImageBitmap? {
    if (!File(ruta).exists()) return null
    val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
    val original = BitmapFactory.decodeFile(ruta, opciones) ?: return null

    val grados = try {
        when (
            ExifInterface(ruta).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    } catch (e: IOException) {
        0f
    }

    val final: Bitmap = if (grados != 0f) {
        val matriz = Matrix().apply { postRotate(grados) }
        Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
    } else {
        original
    }
    return final.asImageBitmap()
}