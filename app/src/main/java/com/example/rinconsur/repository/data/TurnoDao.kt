package com.example.rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.rinconsur.repository.model.Turno
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Insert
    suspend fun insertar(turno: Turno): Long

    @Query("UPDATE turnos SET fin = :fin WHERE id = :id")
    suspend fun finalizar(id: Long, fin: Long)

    @Query(
        """SELECT * FROM turnos
           WHERE usuarioId = :usuarioId AND fin IS NULL
           ORDER BY inicio DESC LIMIT 1"""
    )
    fun turnoActivo(usuarioId: Long): Flow<Turno?>

    @Query("SELECT * FROM turnos ORDER BY inicio DESC")
    fun historial(): Flow<List<Turno>>

    @Query("SELECT * FROM turnos WHERE usuarioId = :usuarioId ORDER BY inicio DESC")
    fun historialDeUsuario(usuarioId: Long): Flow<List<Turno>>

    @Query("SELECT COUNT(*) FROM turnos")
    suspend fun contar(): Int

    // Datos de ejemplo: sus turnos se marcan con un texto fijo en la ruta de la foto
    @Query("SELECT COUNT(*) FROM turnos WHERE fotoRuta = :marca")
    fun contarPorFoto(marca: String): Flow<Int>

    @Query("DELETE FROM turnos WHERE fotoRuta = :marca")
    suspend fun eliminarPorFoto(marca: String)
}