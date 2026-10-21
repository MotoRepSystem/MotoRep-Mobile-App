package com.example.motorep_mobile_app.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.motorep_mobile_app.data.entity.Producto
import kotlinx.coroutines.flow.Flow


@Dao
interface Producto_Dao {

    @Query("SELECT * FROM productos_catalogo ORDER BY producto")
    fun obtenerProductos(): Flow<List<Producto>>   // SIN suspend

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductos(items: List<Producto>)

    @Query("DELETE FROM productos_catalogo")
    suspend fun borrarProductos()

    @Transaction
    suspend fun reemplazarProductos(items: List<Producto>) {
        borrarProductos()
        insertarProductos(items)
    }
}
