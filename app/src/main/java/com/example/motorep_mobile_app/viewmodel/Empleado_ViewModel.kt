package com.example.motorep_mobile_app.viewmodel
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.repository.Empleado_Repository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class Empleado_ViewModel(private val repository: Empleado_Repository):ViewModel(){  // Creamos una clase
    fun guardarEmpleado(empleado: Empleado)   // Creamos una función guardar empleado
    {
        viewModelScope.launch{repository.insertar(empleado)}    // accedemos a la función insertar del repository
    }

    suspend fun actualizarEmpleado(empleado: Empleado) {
        repository.actualizar(empleado)
    }// accedemos a la función actualizar del repository

    val Empleados = repository.listaEmpleado  // Toma la lista que traemos desde el repository

    // Paso EXTRA: Agregamos la función para eliminar un Empleado desde el ViewModel
    fun eliminarEmpleado(empleado: Empleado) {
        viewModelScope.launch {
            repository.eliminar(empleado)
        }
    }

    // ---- Funciones para sincronización diferida (offline-first) ----

    // Obtiene los Empleados creados/editados offline, aún no enviados a la API
    suspend fun obtenerPendientesCrear(): List<Empleado> {
        return repository.obtenerPendientesCrear()
    }

    // Obtiene los Empleados marcados para eliminar, pendientes de confirmar en la API
    suspend fun obtenerPendientesEliminar(): List<Empleado> {
        return repository.obtenerPendientesEliminar()
    }

    // Marca un Empleado como sincronizado exitosamente con la API
    suspend fun marcarComoSincronizado(syncId: String, serverId: Int) {
        repository.marcarComoSincronizado(syncId, serverId)
    }

    // Elimina definitivamente un Empleado de Room, tras confirmar el borrado en la API
    suspend fun eliminarPorId(id: Int) {
        repository.eliminarPorId(id)
    }

    suspend fun sincronizarDesdeAzure(Empleado: Empleado) {
        repository.sincronizarDesdeAzure(Empleado)
    }
    suspend fun eliminarEmpleadosQueYaNoExistenEnAzure(
        EmpleadosAzure: List<Empleado>
    ) {
        repository.eliminarEmpleadosQueYaNoExistenEnAzure(EmpleadosAzure)
    }
}