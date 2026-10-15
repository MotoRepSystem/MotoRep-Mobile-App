package com.example.motorep_mobile_app.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.example.motorep_mobile_app.data.entity.Cliente
import androidx.lifecycle.LiveData

@Dao
interface  ClienteDao {
    @Insert
    suspend fun Insertar(cliente: Cliente)
    @Update
    suspend fun Actualizar(cliente: Cliente)
    @Delete
    suspend fun Eliminar(cliente: Cliente)

    @Query("SELECT * FROM clientes ORDER BY id DESC")
    fun ObtenerTodos(): LiveData<List<Cliente>>


    @Query("SELECT * FROM clientes WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientesCrear(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE pendienteEliminar = 1")
    suspend fun obtenerPendientesEliminar(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientesSincronizacion(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE syncId = :syncId LIMIT 1")
    suspend fun obtenerPorSyncId(syncId: String): Cliente?

    @Query("UPDATE clientes SET sincronizado = 1, serverId = :serverId WHERE syncId = :syncId")
    suspend fun marcarComoSincronizado(syncId: String, serverId: Int)

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("SELECT * FROM clientes WHERE sincronizado = 1 AND pendienteEliminar = 0")
    suspend fun obtenerClientesSincronizados(): List<Cliente>
}