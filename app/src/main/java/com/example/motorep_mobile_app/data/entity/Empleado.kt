package com.example.motorep_mobile_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empleados")
data class Empleado (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val Nombres: String = "",
    val Apellidos: String = "",
    val Telefono: String = "",
    val NumCedula: String = ""

)