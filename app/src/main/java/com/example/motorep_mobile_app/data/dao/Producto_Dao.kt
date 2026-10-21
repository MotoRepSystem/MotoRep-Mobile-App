package com.example.motorep_mobile_app.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motorep_mobile_app.data.entity.Producto
import kotlinx.coroutines.flow.Flow


@Dao
interface Producto_Dao {
    @Query("SELECT * FROM productos ORDER BY detalle")
    fun obtenerProductos(): Flow<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductos(items: List<Producto>)

    @Query("DELETE FROM productos")
    suspend fun borrarProductos()

    @Query("SELECT * FROM productos WHERE id = :id")
    fun observarPorId(id: Int): Flow<Producto?>   // sin suspend, es un Flow
}
