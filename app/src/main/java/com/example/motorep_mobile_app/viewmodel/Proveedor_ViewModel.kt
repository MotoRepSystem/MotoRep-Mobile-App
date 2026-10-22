package com.example.motorep_mobile_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motorep_mobile_app.data.entity.Proveedor
import com.example.motorep_mobile_app.data.repository.Proveedor_Repository
import kotlinx.coroutines.launch

class Proveedor_Viewmodel(private val repository: Proveedor_Repository):ViewModel(){   // Creamos una clase
    fun guardarProveedor(proveedor: Proveedor)   // Creamos una función guardar proveedor
    {
        viewModelScope.launch{repository.insertar(proveedor)}    // accedemos a la función insertar del repository
    }

    suspend fun actualizarProveedor(proveedor: Proveedor) {
        repository.actualizar(proveedor)
    }// accedemos a la función actualizar del repository

    val proveedores = repository.listaProveedor  // Toma la lista que traemos desde el repository

    // Paso EXTRA: Agregamos la función para eliminar un cliente desde el ViewModel
    fun eliminarProveedor(proveedor: Proveedor) {
        viewModelScope.launch {
            repository.eliminar(proveedor)
        }
    }

    // ---- Funciones para sincronización diferida (offline-first) ----

    // Obtiene los proveedores creados/editados offline, aún no enviados a la API
    suspend fun obtenerPendientesCrear(): List<Proveedor> {
        return repository.obtenerPendientesCrear()
    }

    // Obtiene los proveedores marcados para eliminar, pendientes de confirmar en la API
    suspend fun obtenerPendientesEliminar(): List<Proveedor> {
        return repository.obtenerPendientesEliminar()
    }

    // Marca un proveedor como sincronizado exitosamente con la API
    suspend fun marcarComoSincronizado(syncId: String, serverId: Int) {
        repository.marcarComoSincronizado(syncId, serverId)
    }

    // Elimina definitivamente un proveedor de Room, tras confirmar el borrado en la API
    suspend fun eliminarPorId(id: Int) {
        repository.eliminarPorId(id)
    }

    suspend fun sincronizarDesdeAzure(proveedor: Proveedor) {
        repository.sincronizarDesdeAzure(proveedor)
    }
    suspend fun eliminarProveedoresQueYaNoExistenEnAzure(
        proveedoresAzure: List<Proveedor>
    ) {
        repository.eliminarProveedoresQueYaNoExistenEnAzure(proveedoresAzure)
    }
}