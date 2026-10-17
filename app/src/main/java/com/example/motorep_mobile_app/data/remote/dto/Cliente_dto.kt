package com.example.motorep_mobile_app.data.remote.dto

import com.example.motorep_mobile_app.data.entity.Cliente

data class Cliente_dto (
    val id: Int?,
    val Nombres: String?,
    val Apellidos: String?,
    val NumCedula: String?,
    val NumTelefono: String?,
    val syncId: String?
)

// Mapper: convierte lo que llega de la API (DTO) en un Cliente de Room
fun Cliente_dto.toEntity(): Cliente {
    return Cliente(
        id = 0,
        syncId = syncId ?: java.util.UUID.randomUUID().toString(),
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