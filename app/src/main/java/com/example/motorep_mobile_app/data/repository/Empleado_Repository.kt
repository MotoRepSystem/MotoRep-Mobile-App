package com.example.motorep_mobile_app.data.repository
import com.example.motorep_mobile_app.data.dao.Empleado_Dao
import com.example.motorep_mobile_app.data.entity.Empleado

class Empleado_Repository (private val dao: Empleado_Dao){
    val listaEmpleado = dao.ObtenerTodos()
    suspend fun insertarEmpleado(empleado: Empleado) {
        dao.Insertar(empleado)
    }
    suspend fun actualizarEmpleado(empleado: Empleado) {
        dao.Actualizar(empleado)
    }
    suspend fun eliminarEmpleado(empleado: Empleado) {
        dao.Eliminar(empleado)
    }

}