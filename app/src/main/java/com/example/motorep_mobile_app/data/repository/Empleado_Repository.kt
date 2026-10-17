package com.example.motorep_mobile_app.data.repository
import com.example.motorep_mobile_app.data.dao.EmpleadoDao
import com.example.motorep_mobile_app.data.entity.Empleado

class Empleado_Repository (private val dao: EmpleadoDao){
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