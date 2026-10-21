package com.example.motorep_mobile_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val id: Int,
    val cantidad: Int,
    val precioCosto: String,
    val precioVenta: String,
    val fechaRegistro: String,
    val detalle: String
)
