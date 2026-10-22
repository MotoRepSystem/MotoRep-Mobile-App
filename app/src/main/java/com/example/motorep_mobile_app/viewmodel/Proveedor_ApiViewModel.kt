package com.example.motorep_mobile_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motorep_mobile_app.data.remote.dto.Empleado_dto
import com.example.motorep_mobile_app.data.remote.dto.Proveedor_dto
import com.example.motorep_mobile_app.data.repository.Empleado_ApiRepository
import com.example.motorep_mobile_app.data.repository.Proveedor_ApiRepository
import kotlinx.coroutines.launch

class Proveedor_ApiViewModel(
    private val repository: Proveedor_ApiRepository
) : ViewModel() {

    var proveedores: List<Proveedor_dto> = emptyList()
        private set

    fun obtenerProveedores(onResult: (List<Proveedor_dto>) -> Unit) {
        viewModelScope.launch {
            try {
                proveedores = repository.obtenerProveedores()
                onResult(proveedores)
            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(
                        "ProveedorApiViewModel",
                        "HTTP Error ${e.code()}: $errorBody"
                    )
                } else {
                    Log.e(
                        "ProveedorApiViewModel",
                        "Error al obtener proveedores: ${e.message}",
                        e
                    )
                }
            }
        }
    }

    fun guardarProveedor(
        proveedor: Proveedor_dto,
        onResult: (Proveedor_dto?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val respuesta = repository.guardarProveedor(proveedor)
                onResult(respuesta)
            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(
                        "ProveedorApiViewModel",
                        "HTTP Error ${e.code()}: $errorBody"
                    )
                } else {
                    Log.e(
                        "ProveedorApiViewModel",
                        "Error al guardar proveedor: ${e.message}",
                        e
                    )
                }

                onResult(null)
            }
        }
    }

    fun actualizarProveedor(
        id: Int,
        proveedor: Proveedor_dto,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.actualizarProveedor(id, proveedor)
                onResult(true)
            } catch (e: Exception) {
                Log.e(
                    "ProveedorApiViewModel",
                    "Error al actualizar proveedor: ${e.message}"
                )

                onResult(false)
            }
        }
    }

    fun eliminarProveedor(
        id: Int,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.eliminarProveedor(id)
                onResult(true)
            } catch (e: Exception) {
                Log.e(
                    "ProveedorApiViewModel",
                    "Error al eliminar proveedor: ${e.message}"
                )

                onResult(false)
            }
        }
    }

}