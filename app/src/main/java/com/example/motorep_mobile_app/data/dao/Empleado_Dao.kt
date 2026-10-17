
package com.example.motorep_mobile_app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.motorep_mobile_app.data.entity.Empleado

@Dao
interface EmpleadoDao {

    @Insert
    suspend fun Insertar(empleado: Empleado)

    @Update
    suspend fun Actualizar(empleado: Empleado)

    @Delete
    suspend fun Eliminar(empleado: Empleado)

    @Query("SELECT * FROM empleados ORDER BY id DESC")
    fun ObtenerTodos(): LiveData<List<Empleado>>
}
