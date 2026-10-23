
package com.example.motorep_mobile_app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Representa un tipo de marca recibido desde Django.
data class TipoMarca_dto(

    // Identificador del tipo.
    @SerializedName("id")
    val id: Int,

    // Nombre del tipo de marca.
    @SerializedName("TipoMarca")
    val tipoMarca: String
)
