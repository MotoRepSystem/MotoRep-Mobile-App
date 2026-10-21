package com.example.motorep_mobile_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos_catalogo")
data class Producto(
    @PrimaryKey val id: Int,      // id del detalle en el servidor
    val codigo: String,
    val producto: String,
    val marca: String,
    val moto: String,             // "Modelo Año"
    val color: String,
    val size: String
)