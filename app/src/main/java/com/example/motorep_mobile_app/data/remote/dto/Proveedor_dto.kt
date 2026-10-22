package com.example.motorep_mobile_app.data.remote.dto

import com.example.motorep_mobile_app.data.entity.Proveedor
import com.google.gson.annotations.SerializedName

data class Proveedor_dto(
    @SerializedName("id", alternate = ["Id", "idProveedor", "IdProveedor", "id_proveedor"])
    val id: Int? = null,
    @SerializedName("syncId", alternate = ["SyncId", "sync_id"])
    val syncId: String? = null,
    @SerializedName("Nombre", alternate = ["nombre", "Nombres", "nombres"])
    val nombre: String? = null,
    @SerializedName("Telefono", alternate = ["Teléfono", "NumTeléfono", "NumTelefono", "telefono", "numtelefono", "num_telefono"])
    val telefono: String? = null
)

// Mapper: convierte lo que llega de la API (DTO) en un Proveedor de Room
fun Proveedor_dto.toEntity(): Proveedor {
    return Proveedor(
        id = 0,
        syncId = syncId ?: java.util.UUID.randomUUID().toString(),
        serverId = id,
        nombre = nombre.orEmpty(),
        telefono = telefono.orEmpty(),
        sincronizado = true
    )
}

// Mapper inverso: convierte un Proveedor de Room en un DTO para enviarlo a la API
fun Proveedor.toDto(): Proveedor_dto {
    return Proveedor_dto(
        id = serverId,
        syncId = syncId,
        nombre = nombre,
        telefono = telefono
    )
}