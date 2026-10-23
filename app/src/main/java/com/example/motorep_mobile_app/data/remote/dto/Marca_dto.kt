package com.example.motorep_mobile_app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Representa una marca recibida o enviada a Django.
data class Marca_dto(

    // Identificador de la marca.
    @SerializedName("id")
    val id: Int? = null,

    // Nombre de la marca.
    @SerializedName("Nombre")
    val nombre: String,

    // ID del tipo seleccionado al registrar o editar.
    // Django lo recibe, pero no lo devuelve en la respuesta.
    @SerializedName("TipoId")
    val tipoId: Int? = null,

    // Nombre del tipo que Django devuelve al consultar.
    @SerializedName("tipo_nombre")
    val tipoNombre: String? = null
)
