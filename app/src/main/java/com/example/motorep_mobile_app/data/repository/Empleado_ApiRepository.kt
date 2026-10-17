package com.example.motorep_mobile_app.data.repository
import com.example.motorep_mobile_app.data.remote.api.ApiService
import com.example.motorep_mobile_app.data.remote.dto.Empleado_dto

class Empleado_ApiRepository (private val Api : ApiService){
    suspend fun obtenerEmpleado(): List<Empleado_dto> {
        return Api.obtenerEmpleado()
    }

    suspend fun guardarEmpleado(empleado: Empleado_dto): Empleado_dto? {
        val respuesta = Api.guardarEmpleado(empleado)
        return respuesta.firstOrNull() ?: empleado
    }

    suspend fun actualizarEmpleado(id: Int, empleado: Empleado_dto): Empleado_dto? {
        val respuesta = Api.actualizarEmpleado(id, empleado)
        return respuesta.firstOrNull() ?: empleado
    }

    suspend fun eliminarEmpleado(id: Int) {
        return Api.eliminarEmpleado(id)
    }
}