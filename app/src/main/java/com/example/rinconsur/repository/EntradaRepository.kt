package com.example.rinconsur.repository

import com.example.rinconsur.repository.model.Entrada
import com.example.rinconsur.repository.remote.ApiClient
import com.example.rinconsur.repository.remote.api.EntradasApi
import com.example.rinconsur.repository.remote.dto.toModel
import com.example.rinconsur.repository.remote.llamarApi
import java.util.concurrent.ConcurrentHashMap

/** Acceso a ms-entradas. */
class EntradaRepository(
    private val api: EntradasApi = ApiClient.entradasApi
) {
    suspend fun listar(soloActivas: Boolean): Result<List<Entrada>> =
        llamarApi { api.listar(soloActivas).map { it.toModel() } }
            .onSuccess { lista -> NombresEntrada.guardar(lista) }
}

/**
 * Nombres de las entradas que llegaron del backend.
 * Así cualquier pantalla puede mostrar "Entrada Sodimac" a partir del id.
 */
object NombresEntrada {
    private val nombres = ConcurrentHashMap<Long, String>()

    fun guardar(lista: List<Entrada>) {
        lista.forEach { nombres[it.id] = it.nombre }
    }

    fun nombre(id: Long): String? = nombres[id]
}