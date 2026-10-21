package com.example.motorep_mobile_app.data.repository

import com.example.motorep_mobile_app.data.dao.Producto_Dao
import com.example.motorep_mobile_app.data.entity.Producto
import com.example.motorep_mobile_app.data.remote.api.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

suspend fun sincronizarProductos(api: ApiService, dao: Producto_Dao) = coroutineScope {
    val detalles = async { api.obtenerDetalleProductos() }
    val productos = async { api.obtenerProductos().associateBy { it.id } }
    val marcas = async { api.obtenerMarcas().associateBy { it.id } }
    val motos = async { api.obtenerMoto().associateBy { it.id } }
    val colores = async { api.obtenerColor().associateBy { it.id } }
    val sizes = async { api.obtenerTamaños().associateBy { it.id } }

    val p = productos.await(); val m = marcas.await(); val mo = motos.await()
    val c = colores.await(); val s = sizes.await()

    val items = detalles.await().map { d ->
        val moto = mo[d.motoId]
        Producto(
            id = d.id,
            codigo = d.codigo ?: "",
            producto = p[d.productoId]?.Nombre ?: "",
            marca = m[d.marcaId]?.Nombre ?: "",
            moto = moto?.let { "${it.Modelo ?: ""} ${it.Año ?: ""}".trim() } ?: "",
            color = c[d.colorId]?.Color ?: "",
            size = s[d.sizeId]?.Tamaño ?: ""
        )
    }
    dao.reemplazarProductos(items)
}

fun obtenerProductos(dao: Producto_Dao): Flow<List<Producto>> = dao.obtenerProductos()