package com.example.motorep_mobile_app.data.repository
import com.example.motorep_mobile_app.data.remote.api.ApiService
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto

class Cliente_ApiRepository(
    private val api: ApiService
) {
    suspend fun obtenerClientes() = api.obtenerCliente()

    suspend fun guardarClientes(cliente: Cliente_dto) = api.guardarCliente(cliente)

    suspend fun actualizarCliente(id: Int, cliente: Cliente_dto) =
        api.actualizarCliente(id, cliente)

    suspend fun eliminarCliente(id: Int) = api.eliminarCliente(id)
}