package com.example.motorep_mobile_app.data.repository
import com.example.motorep_mobile_app.data.remote.api.ApiService
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto

class Cliente_ApiRepository(private val Api : ApiService) {

    suspend fun obtenerCliente(): List<Cliente_dto> {
        return Api.obtenerCliente()
    }

    suspend fun guardarCliente(cliente: Cliente_dto): Cliente_dto {
        return Api.guardarCliente(cliente)
    }

    suspend fun actualizarCliente(id: Int, cliente: Cliente_dto): Cliente_dto {
        return Api.actualizarCliente(id, cliente)
    }

    suspend fun eliminarCliente(id: Int) {
        return Api.eliminarCliente(id)
    }

}