package com.example.rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.rinconsur.repository.model.PerfilEtario
import com.example.rinconsur.repository.model.PerfilGrupo
import com.example.rinconsur.repository.model.Registro
import com.example.rinconsur.repository.model.ResumenAcceso
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    // CREATE
    @Insert
    suspend fun insertar(registro: Registro): Long

    @Insert
    suspend fun insertarTodos(registros: List<Registro>)

    // UPDATE
    @Update
    suspend fun actualizar(registro: Registro)

    // DELETE
    @Delete
    suspend fun eliminar(registro: Registro)

    // READ
    @Query("SELECT * FROM registros WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Registro?

    @Query("SELECT COUNT(*) FROM registros")
    suspend fun contar(): Int

    @Query("SELECT * FROM registros ORDER BY fechaHora DESC")
    fun historial(): Flow<List<Registro>>

    @Query("SELECT * FROM registros WHERE turnoId = :turnoId ORDER BY fechaHora DESC")
    fun registrosDelTurno(turnoId: Long): Flow<List<Registro>>

    @Query("SELECT COUNT(*) FROM registros WHERE turnoId = :turnoId")
    fun contarDelTurno(turnoId: Long): Flow<Int>

    // Métricas
    @Query(
        """SELECT accesoId, compro,
                  COUNT(*) AS grupos,
                  SUM(1 + acompanantesAdultos + acompanantesNinos) AS personas
           FROM registros
           WHERE fechaHora BETWEEN :desde AND :hasta
           GROUP BY accesoId, compro"""
    )
    fun resumenPorAcceso(desde: Long, hasta: Long): Flow<List<ResumenAcceso>>

    @Query(
        """SELECT rangoEtario, COUNT(*) AS total
           FROM registros
           WHERE compro = :compro AND fechaHora BETWEEN :desde AND :hasta
           GROUP BY rangoEtario"""
    )
    fun perfilEtario(desde: Long, hasta: Long, compro: Boolean): Flow<List<PerfilEtario>>

    @Query(
        """SELECT acompanantesAdultos, acompanantesNinos, COUNT(*) AS total
           FROM registros
           WHERE compro = :compro AND fechaHora BETWEEN :desde AND :hasta
           GROUP BY acompanantesAdultos, acompanantesNinos"""
    )
    fun composicionGrupos(desde: Long, hasta: Long, compro: Boolean): Flow<List<PerfilGrupo>>
}