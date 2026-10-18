package com.example.motorep_mobile_app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.motorep_mobile_app.data.entity.Cliente

@Dao
interface Cliente_Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun Insertar(cliente: Cliente)

    @Update suspend fun Actualizar(cliente: Cliente)

    @Delete suspend fun Eliminar(cliente: Cliente)

    @Query("SELECT * FROM Clientes WHERE pendienteEliminar = 0 ORDER BY id DESC")
    fun ObtenerTodos(): LiveData<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientesCrear(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE pendienteEliminar = 1")
    suspend fun obtenerPendientesEliminar(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientesSincronizacion(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE syncId = :syncId LIMIT 1")
    suspend fun obtenerPorSyncId(syncId: String): Cliente?

    @Query("SELECT * FROM clientes WHERE serverId = :serverId LIMIT 1")
    suspend fun obtenerPorServerId(serverId: Int): Cliente?

    @Query("SELECT * FROM clientes WHERE cedula = :cedula AND serverId IS NULL AND sincronizado = 0 AND pendienteEliminar = 0 LIMIT 1")
    suspend fun obtenerPendientePorCedula(cedula: String): Cliente?

    @Query("UPDATE clientes SET sincronizado = 1, serverId = :serverId WHERE syncId = :syncId")
    suspend fun marcarComoSincronizado(syncId: String, serverId: Int)

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("SELECT * FROM clientes WHERE sincronizado = 1 AND pendienteEliminar = 0")
    suspend fun obtenerClientesSincronizados(): List<Cliente>
}