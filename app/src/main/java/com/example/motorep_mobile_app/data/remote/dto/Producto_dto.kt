package com.example.motorep_mobile_app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class Producto_dto(
    val id: Int,
    @SerializedName("Nombre") val Nombre: String?,
    @SerializedName("CategoriaId") val CategoriaId: Int?,
)
data class Marca_dto(
    val id: Int,
    @SerializedName("Nombre") val Nombre: String?,
    @SerializedName("TipoId") val TipoId: Int?
)
data class Moto_dto(
    val id: Int,
    @SerializedName("Modelo") val Modelo: String?,
    @SerializedName("Año") val Año: Int?,
    @SerializedName("MarcaId") val MarcaId: Int?
)
data class Color_dto(
    val id: Int,
    @SerializedName("Color") val Color: String?
)
data class Size_dto(
    val id:Int,
    @SerializedName("Tamaño") val Tamaño: String?
)
data class DetalleProducto_dto(
    val id: Int,
    @SerializedName("producto") val productoId: Int?,
    @SerializedName("MarcaId") val marcaId: Int?,
    @SerializedName("MotoId") val motoId: Int?,
    @SerializedName("ColorId") val colorId: Int?,
    @SerializedName("Size") val sizeId: Int?,val size: Int?,
    @SerializedName("Codigo") val codigo: String?
)