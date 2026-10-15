package com.example.motorep_mobile_app.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motorep_mobile_app.data.entity.Cliente
import kotlinx.coroutines.launch
import com.example.motorep_mobile_app.data.repository.Cliente_ApiRepository
import com.example.motorep_mobile_app.data.remote.dto.Cliente_dto

class Cliente_ApiViewModel(
    private val repository: Cliente_ApiRepository
) : ViewModel() {

    var clientes: List<Cliente_dto> = emptyList()
        private set

    fun obtenerClientes(onResult: (List<Cliente_dto>) -> Unit) {
        viewModelScope.launch {
            try {
                clientes = repository.obtenerCliente()
                onResult(clientes)
            } catch (e: Exception) {
                Log.e(
                    "ClienteApiViewModel",
                    "Error al obtener clientes: ${e.message}"
                )
            }
        }
    }

    fun guardarCliente(
        cliente: Cliente_dto,
        onResult: (Cliente_dto?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val respuesta = repository.guardarCliente(cliente)
                onResult(respuesta)
            } catch (e: Exception) {
                Log.e(
                    "ClienteApiViewModel",
                    "Error al guardar cliente: ${e.message}"
                )

                onResult(null)
            }
        }
    }

    fun actualizarCliente(
        id: Int,
        cliente: Cliente_dto,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.actualizarCliente(id, cliente)
                onResult(true)
            } catch (e: Exception) {
                Log.e(
                    "ClienteApiViewModel",
                    "Error al actualizar cliente: ${e.message}"
                )

                onResult(false)
            }
        }
    }

    fun eliminarCliente(
        id: Int,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.eliminarCliente(id)
                onResult(true)
            } catch (e: Exception) {
                Log.e(
                    "ClienteApiViewModel",
                    "Error al eliminar cliente: ${e.message}"
                )

                onResult(false)
            }
        }
    }

}