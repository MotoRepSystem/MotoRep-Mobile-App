package com.example.motorep_mobile_app.viewmodel
import com.example.motorep_mobile_app.data.entity.Cliente
import com.example.motorep_mobile_app.data.repository.Cliente_Repository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class Cliente_Viewmodel (private val repository: Cliente_Repository):ViewModel(){
    fun guardarCliente(cliente: Cliente)   // Creamos una función guardar producto
    {
        viewModelScope.launch{repository.insertarCliente(cliente)}    // accedemos a la función insertar del repository
    }

    fun actualizarCliente(cliente: Cliente) {
        viewModelScope.launch {
            repository.actualizarCliente(cliente)
        }
    }// accedemos a la función actualizar del repository

    val cliente = repository.listaClientes  // Toma la lista que traemos desde el repository()  // Toma la lista que traemos desde el repository

   //función para eliminar un producto desde el ViewModel
    fun eliminarCliente(cliente: Cliente) {
        viewModelScope.launch {
            repository.eliminarCliente(cliente)
        }
    }

    // Elimina definitivamente un producto de Room, tras confirmar el borrado en la API
    fun eliminarPorId(id: Int) {
        viewModelScope.launch {
            repository.eliminarPorId(id)
        }
    }


    // ---- Funciones para sincronización diferida (offline-first) ----

    // Obtiene los productos creados/editados offline, aún no enviados a la API
    suspend fun obtenerPendientesCrear(): List<Cliente> {
        return repository.obtenerPendientesCrear()
    }

    // Obtiene los productos marcados para eliminar, pendientes de confirmar en la API
    suspend fun obtenerPendientesEliminar(): List<Cliente> {
        return repository.obtenerPendientesEliminar()
    }

    // Marca un producto como sincronizado exitosamente con la API
    fun marcarComoSincronizado(syncId: String, serverId: Int) {
        viewModelScope.launch {
            repository.marcarComoSincronizado(syncId, serverId)
        }
    }

    suspend fun sincronizarDesdeAzure(cliente: Cliente) {
        repository.sincronizarDesdeAzure(cliente)
    }
    suspend fun eliminarClientesQueYaNoExistenEnAzure(
        clienteAzure: List<Cliente>
    ) {
        repository.eliminarClientesQueYaNoExistenEnAzure(clienteAzure)
    }
}