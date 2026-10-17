package com.example.motorep_mobile_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class Empleado_dto (
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("Nombres")
    val Nombres: String?,
    @SerializedName("Apellidos")
    val Apellidos: String?,
    @SerializedName("Telefono")
    val Telefono: String?,
    @SerializedName("NumCedula")
    val NumCedula: String?
)
