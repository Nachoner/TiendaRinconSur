package com.example.rinconsur.repository.model

// Un usuario junto con su tipo, para mostrar en la lista de usuarios
data class UsuarioConTipo(
    val usuario: Usuario,
    val tipo: TipoUsuario
)