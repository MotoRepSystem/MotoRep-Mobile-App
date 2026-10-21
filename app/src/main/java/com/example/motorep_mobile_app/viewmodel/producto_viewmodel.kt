package com.example.motorep_mobile_app.viewmodel
import android.util.Log
import com.example.motorep_mobile_app.data.entity.Producto
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motorep_mobile_app.data.dao.Producto_Dao
import com.example.motorep_mobile_app.data.remote.api.ApiService
import com.example.motorep_mobile_app.data.repository.obtenerProductos
import com.example.motorep_mobile_app.data.repository.sincronizarProductos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductoViewModel(
    private val api: ApiService,
    private val dao: Producto_Dao
) : ViewModel() {

    val productos: StateFlow<List<Producto>> = obtenerProductos(dao)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun refrescar() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                sincronizarProductos(api, dao)
            } catch (e: retrofit2.HttpException) {
                _error.value = "El servidor respondió ${e.code()}"
                Log.e("SYNC", "HTTP ${e.code()} -> ${e.response()?.errorBody()?.string()}")
            } catch (e: Exception) {
                _error.value = "No se pudo actualizar. Mostrando datos guardados."
                Log.e("SYNC", "Fallo al sincronizar productos", e)
            } finally {
                _cargando.value = false
            }
        }
    }
}
