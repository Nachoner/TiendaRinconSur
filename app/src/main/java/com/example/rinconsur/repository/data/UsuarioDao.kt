package com.example.rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.rinconsur.repository.model.Usuario
import com.example.rinconsur.repository.model.UsuarioConTipo
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // READ
    @Transaction
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun listar(): Flow<List<UsuarioConTipo>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun buscarPorId(id: Long): Usuario?

    // Para el login: el ViewModel compara el hash de la clave
    @Transaction
    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioConTipo?

    // CREATE (ABORT: el email es único)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    // UPDATE
    @Update
    suspend fun actualizar(usuario: Usuario)

    // En vez de borrar, se desactiva: los turnos apuntan al usuario
    @Query("UPDATE usuarios SET activo = :activo WHERE id = :id")
    suspend fun cambiarActivo(id: Long, activo: Boolean)
}