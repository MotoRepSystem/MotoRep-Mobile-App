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

    // Paso EXTRA: Agregamos la función para eliminar un producto desde el ViewModel
    fun eliminarCliente(cliente: Cliente) {
        viewModelScope.launch {
            repository.eliminarCliente(cliente)
        }
    }
}