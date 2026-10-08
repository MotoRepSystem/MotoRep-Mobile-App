package com.example.motorep_mobile_app.viewmodel
import com.example.motorep_mobile_app.data.entity.Producto
import com.example.motorep_mobile_app.data.repository.Producto_Repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class producto_viemodel(private val repository: Producto_Repository):ViewModel(){
    fun guardarProducto(producto: Producto)
    {
        //viewModelScope.launch{repository.Insertar(producto)}
    }
}
