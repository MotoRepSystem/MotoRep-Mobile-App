package com.example.motorep_mobile_app.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import com.example.motorep_mobile_app.data.entity.Producto


@Dao
interface ProductoDao {
    @Insert suspend fun Insertar(producto: Producto)
    @Update suspend fun Actualizar(producto: Producto)
    @Delete suspend fun Eliminar(producto: Producto)
    //linea sin completar
    //@Query("SELECT * FROM Productos") suspend fun ObtenerTodos(): List<Producto>
}

