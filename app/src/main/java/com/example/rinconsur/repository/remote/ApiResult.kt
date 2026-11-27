package com.example.rinconsur.repository.remote

import com.example.rinconsur.repository.remote.dto.ErrorDto
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/** Error devuelto por el backend o por la red, con un mensaje listo para mostrar. */
class ApiException(
    val codigo: Int?,                              // código HTTP, null si no hubo conexión
    mensaje: String,
    val errores: Map<String, String> = emptyMap()  // errores por campo (validaciones)
) : Exception(mensaje)

/**
 * Ejecuta una llamada a la API y devuelve Result:
 * success con el dato, o failure con un ApiException.
 */
suspend fun <T> llamarApi(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (e: HttpException) {
        Result.failure(e.aApiException())
    } catch (e: IOException) {
        Result.failure(ApiException(null, "No se pudo conectar con el servidor. Revisa tu conexión."))
    }

// Lee el JSON {"mensaje": "...", "errores": {...}} que devuelve el backend
private fun HttpException.aApiException(): ApiException {
    val cuerpo = response()?.errorBody()?.string()
    val error = runCatching { Gson().fromJson(cuerpo, ErrorDto::class.java) }.getOrNull()
    return ApiException(
        codigo = code(),
        mensaje = error?.mensaje ?: "Error del servidor (${code()})",
        errores = error?.errores ?: emptyMap()
    )
}