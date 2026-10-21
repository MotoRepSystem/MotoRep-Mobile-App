package com.example.motorep_mobile_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class Producto_dto(
    val id: Int,
    @SerializedName("Cantidad") val cantidad: Int?,
    @SerializedName("precioCosto") val precioCosto: String?,
    @SerializedName("PrecioVenta") val precioVenta: String?,
    @SerializedName("FechaRegistro") val fechaRegistro: String?,
    @SerializedName("detalle_producto") val detalleProducto: String?
)
