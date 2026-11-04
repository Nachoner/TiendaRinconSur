package com.example.rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.rinconsur.repository.model.TipoUsuario
import kotlinx.coroutines.flow.Flow

@Dao
interface TipoUsuarioDao {

    @Query("SELECT * FROM tipos_usuario ORDER BY id ASC")
    fun listar(): Flow<List<TipoUsuario>>

    @Query("SELECT * FROM tipos_usuario WHERE id = :id")
    suspend fun buscarPorId(id: Long): TipoUsuario?

    // ABORT: el nombre es único, si se repite lanza SQLiteConstraintException
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(tipo: TipoUsuario): Long

    @Update
    suspend fun actualizar(tipo: TipoUsuario)

    @Delete
    suspend fun eliminar(tipo: TipoUsuario)
}