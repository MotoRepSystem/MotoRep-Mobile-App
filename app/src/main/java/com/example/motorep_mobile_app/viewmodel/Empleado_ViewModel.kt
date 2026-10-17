package com.example.motorep_mobile_app.viewmodel
import com.example.motorep_mobile_app.data.entity.Empleado
import com.example.motorep_mobile_app.data.repository.Empleado_Repository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class Empleado_ViewModel (private val repository: Empleado_Repository): ViewModel() {
    fun guadarEmpleado(empleado: Empleado){
        viewModelScope.launch { repository.insertarEmpleado(empleado) }
    }
    fun actualizarEmpleado(empleado: Empleado){
        viewModelScope.launch { repository.actualizarEmpleado(empleado) }
    }
    val empleado = repository.listaEmpleado

    fun eliminarEmpleado(empleado: Empleado){
        viewModelScope.launch { repository.eliminarEmpleado(empleado) }
    }
}