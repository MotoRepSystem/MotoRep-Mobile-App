
package com.example.motorep_mobile_app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.motorep_mobile_app.data.entity.Empleado

@Dao
interface Empleado_Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun Insertar(empleado: Empleado)

    @Update suspend fun Actualizar(empleado: Empleado)

    @Delete suspend fun Eliminar(empleado: Empleado)

    @Query("SELECT * FROM empleados WHERE pendienteEliminar = 0 ORDER BY id DESC")
    fun ObtenerTodos(): LiveData<List<Empleado>>

    @Query("SELECT * FROM empleados WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientesCrear(): List<Empleado>

    @Query("SELECT * FROM empleados WHERE pendienteEliminar = 1")
    suspend fun obtenerPendientesEliminar(): List<Empleado>

    @Query("SELECT * FROM empleados WHERE sincronizado = 0 AND pendienteEliminar = 0")
    suspend fun obtenerPendientesSincronizacion(): List<Empleado>

    @Query("SELECT * FROM empleados WHERE syncId = :syncId LIMIT 1")
    suspend fun obtenerPorSyncId(syncId: String): Empleado?

    @Query("SELECT * FROM empleados WHERE serverId = :serverId LIMIT 1")
    suspend fun obtenerPorServerId(serverId: Int): Empleado?

    @Query("SELECT * FROM empleados WHERE cedula = :NumCedula AND serverId IS NULL AND sincronizado = 0 AND pendienteEliminar = 0 LIMIT 1")
    suspend fun obtenerPendientePorCedula(NumCedula: String): Empleado?

    @Query("UPDATE empleados SET sincronizado = 1, serverId = :serverId WHERE syncId = :syncId")
    suspend fun marcarComoSincronizado(syncId: String, serverId: Int)

    @Query("DELETE FROM empleados WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    @Query("SELECT * FROM empleados WHERE sincronizado = 1 AND pendienteEliminar = 0")
    suspend fun obtenerEmpleadosSincronizados(): List<Empleado>
}