package com.example.motorep_mobile_app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.motorep_mobile_app.data.entity.TipoMarcaEntity

@Dao
interface TipoMarca_Dao {

    // Consultar los tipos de marca almacenados localmente.
    @Query("SELECT * FROM tipos_marca ORDER BY tipoMarca ASC")
    fun obtenerTipos(): LiveData<List<TipoMarcaEntity>>

    // Guardar varios tipos de marca.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTipos(tipos: List<TipoMarcaEntity>)

    // Eliminar todos los tipos de marca.
    @Query("DELETE FROM tipos_marca")
    suspend fun eliminarTodos()

    @Transaction
    suspend fun reemplazarTipos(tipos: List<TipoMarcaEntity>) {
        eliminarTodos()
        insertarTipos(tipos)
    }
}

