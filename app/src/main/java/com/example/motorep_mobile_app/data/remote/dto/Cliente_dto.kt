package com.example.motorep_mobile_app.data.remote.dto

import com.example.motorep_mobile_app.data.entity.Cliente
import com.google.gson.annotations.SerializedName

data class Cliente_dto(
    @SerializedName("id", alternate = ["Id", "idCliente", "IdCliente", "id_cliente"])
    val id: Int? = null,
    @SerializedName("syncId", alternate = ["SyncId", "sync_id"])
    val syncId: String? = null,
    @SerializedName("Nombres", alternate = ["nombre", "Nombre", "nombres", "nombresCliente"])
    val nombre: String? = null,
    @SerializedName("Apellidos", alternate = ["apellido", "Apellido", "apellidos", "apellidosCliente"])
    val apellido: String? = null,
    @SerializedName("NumCedula", alternate = ["cedula", "Cedula", "numCedula", "num_cedula"])
    val cedula: String? = null,
    @SerializedName("NumTeléfono", alternate = ["NumTelefono", "Telefono", "telefono", "numtelefono", "num_telefono", "Teléfono"])
    val telefono: String? = null
)

// Mapper: convierte lo que llega de la API (DTO) en un Cliente de Room
fun Cliente_dto.toClienteEntity(): Cliente {
    return Cliente(
        id = 0,
        syncId = syncId ?: java.util.UUID.randomUUID().toString(),
        serverId = id,
        nombre = nombre.orEmpty(),
        apellido = apellido.orEmpty(),
        cedula = cedula.orEmpty(),
        telefono = telefono.orEmpty(),
        sincronizado = true
    )
}

fun Cliente.toClienteDto(): Cliente_dto {
    return Cliente_dto(
        id = serverId,
        syncId = syncId,
        nombre = nombre,
        apellido = apellido,
        cedula = cedula,
        telefono = telefono
    )
}