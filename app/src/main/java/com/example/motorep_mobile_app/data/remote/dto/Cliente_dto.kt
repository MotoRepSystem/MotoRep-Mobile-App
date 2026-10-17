package com.example.motorep_mobile_app.data.remote.dto

import com.example.motorep_mobile_app.data.entity.Cliente
import com.google.gson.annotations.SerializedName

data class Cliente_dto (
    val id: Int?,
    @SerializedName("syncId", alternate = ["SyncId", "sync_id"])
    val syncId: String?,
    val Nombres: String?,
    val Apellidos: String?,
    val NumCedula: String?,
    @SerializedName("NumTelefono", alternate = ["NumTeléfono", "num_telefono", "telefono"])
    val NumTelefono: String?
)

// Mapper: convierte lo que llega de la API (DTO) en un Cliente de Room
fun Cliente_dto.toEntity(): Cliente {
    return Cliente(
        id = 0,
        syncId = if (!syncId.isNullOrBlank()) syncId else java.util.UUID.randomUUID().toString(),
        serverId = id,
        nombre = Nombres ?: "",
        apellido = Apellidos ?: "",
        cedula = NumCedula ?: "",
        telefono = NumTelefono ?: "",
        sincronizado = true
    )
}

// Mapper inverso: convierte un Cliente de Room en un DTO para enviarlo a la API
fun Cliente.toDto(): Cliente_dto {
    return Cliente_dto(
        id = serverId ?: 0,
        syncId = syncId,
        Nombres = nombre,
        Apellidos = apellido,
        NumCedula = cedula,
        NumTelefono = telefono
    )
}