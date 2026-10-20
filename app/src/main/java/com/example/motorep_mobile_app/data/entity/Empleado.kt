package com.example.motorep_mobile_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "empleados")
data class Empleado (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val syncId: String = UUID.randomUUID().toString(),//id de sincronizacion
    val serverId: Int? = null,//id del registro en el servidor
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val cedula: String = "",
    val sincronizado: Boolean = false, // false = pendiente de crear o actualizar en la API
    val pendienteEliminar: Boolean = false // true = hay que borrarlo de la API cuando vuelve la conexión

)