package com.example.motorep_mobile_app.data.remote.dto

import com.example.motorep_mobile_app.data.entity.Cliente

data class Cliente_dto(
    val id: Int?,
    val syncId: String,
    val nombre: String,
    val apellido: String,
    val cedula: String,
    val telefono: String
)

// Mapper: convierte lo que llega de la API (DTO) en un Producto de Room
fun Cliente_dto.toEntity(): Cliente {
    return Cliente(
        id = 0,
        syncId = syncId,
        serverId = id,
        nombre = nombre,
        apellido = apellido,
        cedula = cedula,
        telefono = telefono,
        sincronizado = true
    )
}

// Mapper inverso: convierte un Cliente de Room en un DTO para enviarlo a la API
fun Cliente.toDto(): Cliente_dto {
    return Cliente_dto(
        id = serverId,
        syncId = syncId,
        nombre = nombre,
        apellido = apellido,
        cedula = cedula,
        telefono = telefono
    )
}