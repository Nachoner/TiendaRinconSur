package com.example.rinconsur.repository.remote

/** Direcciones de los microservicios. */
object ApiConfig {
    // En el emulador, 10.0.2.2 es el "localhost" de tu PC
    private const val HOST = "http://localhost"

    const val USUARIOS_URL = "$HOST:8081/"
    const val ENTRADAS_URL = "$HOST:8082/"
    const val TURNOS_URL = "$HOST:8083/"
    const val REGISTROS_URL = "$HOST:8084/"
}