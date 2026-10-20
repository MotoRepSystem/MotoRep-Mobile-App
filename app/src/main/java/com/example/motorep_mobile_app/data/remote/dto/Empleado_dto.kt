package com.example.motorep_mobile_app.data.remote.dto

import com.example.motorep_mobile_app.data.entity.Empleado
import com.google.gson.annotations.SerializedName

data class Empleado_dto(
    @SerializedName("id", alternate = ["Id", "idEmpleado", "IdEmpleado", "id_empleado"])
    val id: Int? = null,
    @SerializedName("syncId", alternate = ["SyncId", "sync_id"])
    val syncId: String? = null,
    @SerializedName("Nombres", alternate = ["nombre", "Nombre", "nombres", "nombresEmpleado"])
    val nombre: String? = null,
    @SerializedName("Apellidos", alternate = ["apellido", "Apellido", "apellidos", "apellidosEmpleado"])
    val apellido: String? = null,
    @SerializedName("Telefono", alternate = ["NumTeléfono","telefono","numtelefono", "num_telefono"])
    val telefono: String? = null,
    @SerializedName("NumCedula", alternate = ["cedula", "Cedula", "numCedula", "num_cedula"])
    val cedula: String? = null
)

// Mapper: convierte lo que llega de la API (DTO) en un Empleado de Room
fun Empleado_dto.toEntity(): Empleado {
    return Empleado(
        id = 0,
        syncId = syncId ?: java.util.UUID.randomUUID().toString(),
        serverId = id,
        nombre = nombre.orEmpty(),
        apellido = apellido.orEmpty(),
        telefono = telefono.orEmpty(),
        cedula = cedula.orEmpty(),
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
        telefono = telefono,
        cedula = cedula
    )
}