package com.example.motorep_mobile_app.viewmodel
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.motorep_mobile_app.data.repository.Empleado_ApiRepository
import com.example.motorep_mobile_app.data.remote.dto.Empleado_dto

class Empleado_ApiViewModel(
    private val repository: Empleado_ApiRepository
) : ViewModel() {

    var empleados: List<Empleado_dto> = emptyList()
        private set

    fun obtenerEmpleados(onResult: (List<Empleado_dto>) -> Unit) {
        viewModelScope.launch {
            try {
                empleados = repository.obtenerEmpleados()
                onResult(empleados)
            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(
                        "EmpleadoApiViewModel",
                        "HTTP Error ${e.code()}: $errorBody"
                    )
                } else {
                    Log.e(
                        "EmpleadoApiViewModel",
                        "Error al obtener empleados: ${e.message}",
                        e
                    )
                }
            }
        }
    }

    fun guardarEmpleado(
        empleado: Empleado_dto,
        onResult: (Empleado_dto?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val respuesta = repository.guardarEmpleado(empleado)
                onResult(respuesta)
            } catch (e: Exception) {
                if (e is retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(
                        "EmpleadoApiViewModel",
                        "HTTP Error ${e.code()}: $errorBody"
                    )
                } else {
                    Log.e(
                        "EmpleadoApiViewModel",
                        "Error al guardar empleado: ${e.message}",
                        e
                    )
                }

                onResult(null)
            }
        }
    }

    fun actualizarEmpleado(
        id: Int,
        empleado: Empleado_dto,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.actualizarEmpleado(id, empleado)
                onResult(true)
            } catch (e: Exception) {
                Log.e(
                    "EmpleadoApiViewModel",
                    "Error al actualizar empleado: ${e.message}"
                )

                onResult(false)
            }
        }
    }

    fun eliminarEmpleado(
        id: Int,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.eliminarEmpleado(id)
                onResult(true)
            } catch (e: Exception) {
                Log.e(
                    "EmpleadoApiViewModel",
                    "Error al eliminar empleado: ${e.message}"
                )

                onResult(false)
            }
        }
    }

}