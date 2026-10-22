package com.example.motorep_mobile_app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.motorep_mobile_app.data.entity.Proveedor


@Dao
interface Proveedor_Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun Insertar(proveedor: Proveedor)

    @Update
    suspend fun Actualizar(proveedor: Proveedor)

    @Delete
    suspend fun Eliminar(proveedor: Proveedor)

    @Query("SELECT * FROM Proveedor WHERE pendienteEliminar = 0 ORDER BY id DESC")
    fun ObtenerTodos(): LiveData<List<Proveedor>>

    @Query("SELECT * FROM proveedor WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientesCrear(): List<Proveedor>

    @Query("SELECT * FROM proveedor WHERE pendienteEliminar = 1")
    suspend fun obtenerPendientesEliminar(): List<Proveedor>

    @Query("SELECT * FROM proveedor WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientesSincronizacion(): List<Proveedor>

    @Query("SELECT * FROM proveedor WHERE syncId = :syncId LIMIT 1")
    suspend fun obtenerPorSyncId(syncId: String): Proveedor?

    @Query("SELECT * FROM proveedor WHERE serverId = :serverId LIMIT 1")
    suspend fun obtenerPorServerId(serverId: Int): Proveedor?

    @Query("UPDATE proveedor SET sincronizado = 1, serverId = :serverId WHERE syncId = :syncId")
    suspend fun marcarComoSincronizado(syncId: String, serverId: Int)

    @Query("DELETE FROM proveedor WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("SELECT * FROM proveedor WHERE sincronizado = 1 AND pendienteEliminar = 0")
    suspend fun obtenerProveedoresSincronizados(): List<Proveedor>
}