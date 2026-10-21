package com.example.motorep_mobile_app.data.repository
import androidx.room.withTransaction
import com.example.motorep_mobile_app.data.database.Motorep_Database
import com.example.motorep_mobile_app.data.entity.Producto
import com.example.motorep_mobile_app.data.remote.api.ApiService
import kotlinx.coroutines.flow.Flow

class Producto_Repository(
    private val api: ApiService,
    private val db: Motorep_Database,
) {
    private val dao = db.productoDao()

    fun obtenerProductos(): Flow<List<Producto>> = dao.obtenerProductos ()

    suspend fun sincronizarInventario() {
        val items = api.obtenerProductos().map {
            Producto(
                id = it.id,
                cantidad = it.cantidad ?: 0,
                precioCosto = it.precioCosto ?: "0.00",
                precioVenta = it.precioVenta ?: "0.00",
                fechaRegistro = it.fechaRegistro ?: "",
                detalle = it.detalleProducto ?: ""
            )
        }
        db.withTransaction {
            dao.borrarProductos()
            dao.insertarProductos(items)
        }
    }
    fun observarProducto(id: Int): Flow<Producto?> = dao.observarPorId(id)
}