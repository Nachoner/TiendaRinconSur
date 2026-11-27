package com.example.rinconsur.repository.model

/** Entrada de la tienda (viene de ms-entradas). */
data class Entrada(
    val id: Long,
    val nombre: String,
    val activa: Boolean = true
)