package com.example.motorep_mobile_app.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.example.motorep_mobile_app.data.entity.Cliente
import androidx.lifecycle.LiveData

@Dao
interface  ClienteDao {
    @Insert suspend fun Insertar(cliente: Cliente)
    @Update suspend fun Actualizar(cliente: Cliente)
    @Delete suspend fun Eliminar(cliente: Cliente)

    @Query("SELECT * FROM clientes ORDER BY id DESC")
    fun ObtenerTodos(): LiveData<List<Cliente>>
}