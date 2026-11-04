package com.example.motorep_mobile_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipos_marca")
data class TipoMarcaEntity(
    @PrimaryKey
    val id: Int,
    val tipoMarca: String
)
