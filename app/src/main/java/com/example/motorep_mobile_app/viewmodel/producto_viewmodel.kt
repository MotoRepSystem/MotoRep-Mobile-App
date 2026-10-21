package com.example.motorep_mobile_app.viewmodel
import android.util.Log
import com.example.motorep_mobile_app.data.entity.Producto
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motorep_mobile_app.data.repository.Producto_Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class Productos_ViewModel(
    private val repository: Producto_Repository
) : ViewModel() {

    val inventario: StateFlow<List<Producto>> = repository.obtenerProductos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _ultimaSync = MutableStateFlow<Long?>(null)
    val ultimaSync: StateFlow<Long?> = _ultimaSync

    fun refrescar() {
        if (_cargando.value) return   // evita dos sincronizaciones simultáneas
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                repository.sincronizarInventario()
                _ultimaSync.value = System.currentTimeMillis()
            } catch (e: retrofit2.HttpException) {
                _error.value = "El servidor respondió ${e.code()}"
                Log.e("SYNC", "HTTP ${e.code()} -> ${e.response()?.errorBody()?.string()}")
            } catch (e: Exception) {
                _error.value = "No se pudo actualizar. Mostrando datos guardados."
                Log.e("SYNC", "Fallo al sincronizar inventario", e)
            } finally {
                _cargando.value = false
            }
        }
    }

    fun limpiarError() { _error.value = null }
}

class ProductoDetalle_ViewModel(
    repository: Producto_Repository,
    id: Int
) : ViewModel() {
    val producto: StateFlow<Producto?> = repository.observarProducto(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}