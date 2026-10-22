package com.example.motorep_mobile_app.data.repository
import com.example.motorep_mobile_app.data.remote.api.ApiService
import com.example.motorep_mobile_app.data.remote.dto.Proveedor_dto

class Proveedor_ApiRepository(
    private val api: ApiService
) {
    suspend fun obtenerProveedores() = api.obtenerProveedores()

    suspend fun guardarProveedor(proveedor: Proveedor_dto) = api.guardarProveedor(proveedor)

    suspend fun actualizarProveedor(id: Int, proveedor: Proveedor_dto) =
        api.actualizarProveedor(id, proveedor)

    suspend fun eliminarProveedor(id: Int) = api.eliminarProveedor(id)
}