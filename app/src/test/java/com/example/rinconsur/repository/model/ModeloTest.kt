package com.example.rinconsur.repository.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prueba unitaria de las reglas de los modelos:
 * tamaño del grupo de una salida y tipo de usuario.
 */
class ModelosTest {

    // Registro de ejemplo; solo cambian los acompañantes
    private fun registro(adultos: Int, ninos: Int) = Registro(
        id = 1,
        turnoId = 1,
        accesoId = 3,
        fechaHora = 0L,
        rangoEtario = "26-35",
        acompanantesAdultos = adultos,
        acompanantesNinos = ninos,
        compro = true
    )

    // Usuario de ejemplo; solo cambia el tipo
    private fun usuario(tipoId: Long) = Usuario(
        id = 1,
        nombre = "Ana Perez",
        email = "ana@duoc.cl",
        tipoUsuarioId = tipoId
    )

    // ---------- Tamaño del grupo ----------

    @Test
    fun tamanoGrupo_personaSola_esUno() {
        assertEquals(1, registro(adultos = 0, ninos = 0).tamanoGrupo)
    }

    @Test
    fun tamanoGrupo_sumaPrincipalMasAcompanantes() {
        // 1 persona principal + 2 adultos + 3 niños = 6
        assertEquals(6, registro(adultos = 2, ninos = 3).tamanoGrupo)
    }

    // ---------- Tipo de usuario ----------

    @Test
    fun usuarioOperador_esOperadorYNoSupervisor() {
        val u = usuario(TiposBase.OPERADOR_ID)
        assertTrue(u.esOperador)
        assertFalse(u.esSupervisor)
    }

    @Test
    fun usuarioSupervisor_esSupervisorYNoOperador() {
        val u = usuario(TiposBase.SUPERVISOR_ID)
        assertTrue(u.esSupervisor)
        assertFalse(u.esOperador)
    }

    @Test
    fun usuarioDeOtroTipo_noEsOperadorNiSupervisor() {
        val u = usuario(5L)   // por ejemplo, "Guardia"
        assertFalse(u.esOperador)
        assertFalse(u.esSupervisor)
    }
}