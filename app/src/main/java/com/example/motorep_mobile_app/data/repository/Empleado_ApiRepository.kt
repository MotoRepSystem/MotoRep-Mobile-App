package com.example.motorep_mobile_app.data.repository
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.example.motorep_mobile_app.data.remote.api.ApiService
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto
import com.example.motorep_mobile_app.data.remote.dto.Empleado_dto

class Empleado_ApiRepository(
    private val api: ApiService
) {
    suspend fun obtenerEmpleados() = api.obtenerEmpleado()

    suspend fun guardarEmpleado(empleado: Empleado_dto) = api.guardarEmpleado(empleado)

    suspend fun actualizarEmpleado(id: Int, empleado: Empleado_dto) =
        api.actualizarEmpleado(id, empleado)

    suspend fun eliminarEmpleado(id: Int) = api.eliminarEmpleado(id)
}