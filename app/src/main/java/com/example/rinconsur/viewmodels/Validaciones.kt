package com.example.rinconsur.viewmodels

import com.example.rinconsur.repository.model.TipoUsuario

/**
 * Reglas de validación compartidas por los ViewModels.
 * Las funciones validarX devuelven el mensaje de error, o null si el dato es válido.
 */
object Validaciones {

    const val MAX_ACOMPANANTES = 10
    const val MIN_CLAVE = 6

    val ACCESOS = 1..4

    val RANGOS_ETARIOS = listOf(
        "Menor de 18", "18-25", "26-35", "36-50", "51-65", "66 o más"
    )

    private const val LETRA = "[A-Za-zÁÉÍÓÚáéíóúÜüÑñ]"

    // Solo letras, con UN espacio entre palabras. Ej.: "Ana Pérez", "Operador"
    private val SOLO_LETRAS_Y_ESPACIOS = Regex("^$LETRA+( $LETRA+)*$")

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun esSoloLetras(valor: String): Boolean = SOLO_LETRAS_Y_ESPACIOS.matches(valor)

    fun esEmailValido(valor: String): Boolean = EMAIL.matches(valor.trim())

    fun validarNombre(valor: String): String? = when {
        valor.isBlank() -> "Ingresa el nombre"
        !esSoloLetras(valor) -> "Solo letras, con un espacio entre palabras"
        else -> null
    }

    fun validarTipoUsuario(valor: String): String? = when {
        valor.isBlank() -> "Ingresa el nombre del tipo"
        valor.length > TipoUsuario.MAX_LARGO -> "Máximo ${TipoUsuario.MAX_LARGO} caracteres"
        !esSoloLetras(valor) -> "Solo letras, con un espacio entre palabras"
        else -> null
    }

    fun validarEmail(valor: String): String? = when {
        valor.isBlank() -> "Ingresa el email"
        !esEmailValido(valor) -> "Email no válido"
        else -> null
    }

    // Deja solo los dígitos 0-9 y limita el largo (las cantidades van de 0 a 10)
    fun soloDigitos(valor: String, maxLargo: Int = 2): String =
        valor.filter { it in '0'..'9' }.take(maxLargo)


    fun validarClave(valor: String): String? = when {
        valor.isBlank() -> "Ingresa la contraseña"
        valor.length < MIN_CLAVE -> "Mínimo $MIN_CLAVE caracteres"
        else -> null
    }

    fun validarAcceso(acceso: Int?): String? = when {
        acceso == null -> "Selecciona un acceso"
        acceso !in ACCESOS -> "Acceso no válido"
        else -> null
    }

    fun validarFoto(ruta: String?): String? =
        if (ruta.isNullOrBlank()) "Toma la foto de la puerta" else null

    fun validarRango(rango: String?): String? = when {
        rango.isNullOrBlank() -> "Selecciona un rango etario"
        rango !in RANGOS_ETARIOS -> "Rango no válido"
        else -> null
    }

    fun validarCantidad(texto: String): String? {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return "Ingresa un número (0 si no hay)"
        val n = limpio.toIntOrNull() ?: return "Debe ser un número entero"
        return when {
            n < 0 -> "No puede ser negativo"
            n > MAX_ACOMPANANTES -> "Máximo $MAX_ACOMPANANTES"
            else -> null
        }
    }

    fun validarCompro(compro: Boolean?): String? =
        if (compro == null) "Indica si sale con bolsa o sin bolsa" else null
}