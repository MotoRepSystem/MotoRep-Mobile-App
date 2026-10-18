package com.example.motorep_mobile_app.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.motorep_mobile_app.data.entity.Cliente  // Importamos la entidad Cliente
import com.example.motorep_mobile_app.data.repository.Cliente_Repository   // Importamos el repository

class Cliente_Viewmodel(private val repository: Cliente_Repository):ViewModel(){   // Creamos una clase
    fun guardarCliente(cliente: Cliente)   // Creamos una función guardar cliente
    {
        viewModelScope.launch{repository.insertar(cliente)}    // accedemos a la función insertar del repository
    }

    suspend fun actualizarCliente(cliente: Cliente) {
        repository.actualizar(cliente)
    }// accedemos a la función actualizar del repository

    val clientes = repository.listaCliente  // Toma la lista que traemos desde el repository

    // Paso EXTRA: Agregamos la función para eliminar un cliente desde el ViewModel
    fun eliminarCliente(cliente: Cliente) {
        viewModelScope.launch {
            repository.eliminar(cliente)
        }
    }

    // ---- Funciones para sincronización diferida (offline-first) ----

    // Obtiene los clientes creados/editados offline, aún no enviados a la API
    suspend fun obtenerPendientesCrear(): List<Cliente> {
        return repository.obtenerPendientesCrear()
    }

    // Obtiene los clientes marcados para eliminar, pendientes de confirmar en la API
    suspend fun obtenerPendientesEliminar(): List<Cliente> {
        return repository.obtenerPendientesEliminar()
    }

    // Marca un cliente como sincronizado exitosamente con la API
    suspend fun marcarComoSincronizado(syncId: String, serverId: Int) {
        repository.marcarComoSincronizado(syncId, serverId)
    }

    // Elimina definitivamente un cliente de Room, tras confirmar el borrado en la API
    suspend fun eliminarPorId(id: Int) {
        repository.eliminarPorId(id)
    }

    suspend fun sincronizarDesdeAzure(cliente: Cliente) {
        repository.sincronizarDesdeAzure(cliente)
    }
    suspend fun eliminarClientesQueYaNoExistenEnAzure(
        clientesAzure: List<Cliente>
    ) {
        repository.eliminarClientesQueYaNoExistenEnAzure(clientesAzure)
    }
}