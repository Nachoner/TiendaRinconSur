package com.example.rinconsur.repository.remote

/** Direcciones de los microservicios. */
object ApiConfig {

    // true = backend en Render (internet) | false = backend en tu PC (con adb reverse)
    private const val USAR_RENDER = true

    val USUARIOS_URL = if (USAR_RENDER) "https://ms-usuarios-1b0a.onrender.com/" else "http://localhost:8081/"
    val ENTRADAS_URL = if (USAR_RENDER) "https://ms-entradas.onrender.com/" else "http://localhost:8082/"
    val TURNOS_URL = if (USAR_RENDER) "https://ms-turnos-19y3.onrender.com/" else "http://localhost:8083/"
    val REGISTROS_URL = if (USAR_RENDER) "https://ms-registros.onrender.com/" else "http://localhost:8084/"
}