package com.example.rinconsur.view.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun obtenerVibrador(context: Context): Vibrator =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

/** Recurso del dispositivo: vibración corta cuando una acción sale bien. */
fun vibrarConfirmacion(context: Context) {
    val vibrador = obtenerVibrador(context)
    if (vibrador.hasVibrator()) {
        vibrador.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

/** Vibración doble cuando un formulario tiene errores. */
fun vibrarError(context: Context) {
    val vibrador = obtenerVibrador(context)
    if (vibrador.hasVibrator()) {
        vibrador.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 70, 80), -1))
    }
}

fun formatearFechaHora(milis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(milis))