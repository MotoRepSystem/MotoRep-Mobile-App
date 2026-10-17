package com.example.motorep_mobile_app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.identity.util.UUID

@Entity(tableName = "marcas") // Definimos el nombre de nuestra tabla como una entidad
data class Marcas( // creamos una clase
    @PrimaryKey(autoGenerate = true) // Le indicamos que la Llave primaria va a ser autoincrementable
    val id:Int = 0, // Atributo Llave primaria
    val syncId: String = UUID.randomUUID().toString(),
    val serverId: Int? = null,
    val nombre:String, // Nombre de la marca
    val TipoId: Int,
    val sincronizado: Boolean = false, // false = pendiente de crear o actualizar en la API
    val pendienteEliminar: Boolean = false // true = hay que borrarlo de la API cuando vuelve la conexión
)
