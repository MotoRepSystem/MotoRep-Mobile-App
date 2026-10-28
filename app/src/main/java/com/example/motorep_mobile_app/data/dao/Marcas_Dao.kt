package com.example.motorep_mobile_app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.motorep_mobile_app.data.entity.Marcas

@Dao
interface Marcas_Dao {

    @Query("SELECT * FROM marcas WHERE pendienteEliminar = 0")
    fun obtenerMarcas(): LiveData<List<Marcas>>

    @Query("SELECT * FROM marcas WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientes(): List<Marcas>

    @Query("SELECT * FROM marcas WHERE pendienteEliminar = 1")
    suspend fun obtenerPendientesEliminar(): List<Marcas>

    @Query("SELECT * FROM marcas WHERE sincronizado = 1 AND pendienteEliminar = 0 AND serverId IS NOT NULL")
    suspend fun obtenerMarcasSincronizadas(): List<Marcas>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(marca: Marcas)

    @Update
    suspend fun actualizar(marca: Marcas)

    @Query("SELECT * FROM marcas WHERE id = :id")
    suspend fun buscarPorId(id: Int): Marcas?

    @Query("SELECT * FROM marcas WHERE serverId = :serverId")
    suspend fun buscarPorServerId(serverId: Int): Marcas?

    @Query("UPDATE marcas SET serverId = :serverId, sincronizado = 1 WHERE id = :id")
    suspend fun marcarComoSincronizada(id: Int, serverId: Int)

    @Query("DELETE FROM marcas WHERE id = :id AND serverId IS NULL")
    suspend fun eliminarLocal(id: Int)

    @Query("DELETE FROM marcas WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}
