package com.example.motorep_mobile_app.data.dao

import androidx.room.*
import com.example.motorep_mobile_app.data.entity.TipoMarcaEntity

@Dao
interface TipoMarca_Dao {

    @Query("SELECT * FROM tipos_marca ORDER BY tipoMarca")
    suspend fun obtenerTipos(): List<TipoMarcaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTipos(tipos: List<TipoMarcaEntity>)

    @Query("DELETE FROM tipos_marca")
    suspend fun eliminarTodos()
}