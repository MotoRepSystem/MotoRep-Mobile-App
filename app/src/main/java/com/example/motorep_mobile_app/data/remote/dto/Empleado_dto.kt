package com.example.motorep_mobile_app.data.remote.dto

import com.example.motorep_mobile_app.data.entity.Empleado
import com.google.gson.annotations.SerializedName

data class Empleado_dto(
    @SerializedName("id", alternate = ["Id"])
    val id: Int?,
    @SerializedName("syncId", alternate = ["SyncId", "sync_id"])
    val syncId: String?,
    @SerializedName("Nombres", alternate = ["nombre", "Nombre", "nombres"])
    val nombre: String?,
    @SerializedName("Apellidos", alternate = ["apellido", "Apellido", "apellidos"])
    val apellido: String?,
    @SerializedName("Teléfono", alternate = ["Telefono", "NumTelefono", "telefono"])
    val telefono: String?,
    @SerializedName("NumCedula", alternate = ["cedula", "Cedula", "numCedula"])
    val cedula: String?

)

// Mapper: convierte lo que llega de la API (DTO) en un Empleado de Room
fun Empleado_dto.toEntity(): Empleado {
    return Empleado(
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

// Mapper inverso: convierte un Empleado de Room en un DTO para enviarlo a la API
fun Empleado.toDto(): Empleado_dto {
    return Empleado_dto(
        id = serverId,
        syncId = syncId,
        nombre = nombre,
        apellido = apellido,
        cedula = cedula,
        telefono = telefono
    )
}