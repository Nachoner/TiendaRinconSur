package com.example.rinconsur.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Prueba unitaria de las validaciones del formulario de login.
 * Las funciones devuelven el mensaje de error, o null si el dato es válido.
 */
class ValidacionesTest {

    // ---------- Email ----------

    @Test
    fun validarEmail_emailCorrecto_noDevuelveError() {
        assertNull(Validaciones.validarEmail("operador@duoc.cl"))
    }

    @Test
    fun validarEmail_vacio_pideIngresarEmail() {
        assertEquals("Ingresa el email", Validaciones.validarEmail(""))
        assertEquals("Ingresa el email", Validaciones.validarEmail("   "))
    }

    @Test
    fun validarEmail_sinArrobaOSinDominio_esInvalido() {
        assertEquals("Email no válido", Validaciones.validarEmail("operadorduoc.cl"))
        assertEquals("Email no válido", Validaciones.validarEmail("operador@duoc"))
    }

    // ---------- Contraseña ----------

    @Test
    fun validarClave_conSeisOMasCaracteres_noDevuelveError() {
        assertNull(Validaciones.validarClave("operador123"))
        assertNull(Validaciones.validarClave("123456"))   // justo el mínimo
    }

    @Test
    fun validarClave_vacia_pideIngresarContrasena() {
        assertEquals("Ingresa la contraseña", Validaciones.validarClave(""))
    }

    @Test
    fun validarClave_muyCorta_indicaElMinimo() {
        assertEquals("Mínimo 6 caracteres", Validaciones.validarClave("12345"))
    }
}