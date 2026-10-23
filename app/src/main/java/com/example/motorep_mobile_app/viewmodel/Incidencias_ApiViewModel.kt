package com.example.motorep_mobile_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motorep_mobile_app.data.incidencias.Incidencia
import com.example.motorep_mobile_app.data.repository.IncidenciaRepository
import kotlinx.coroutines.launch

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class IncidenciaViewModel(
    private val repository: IncidenciaRepository
) : ViewModel() {

    private val _state = MutableLiveData<UiState<List<Incidencia>>>()
    val state: LiveData<UiState<List<Incidencia>>> = _state

    init {
        cargar()
    }

    fun cargar() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            repository.obtenerIncidencias(limit = 100)
                .onSuccess { _state.value = UiState.Success(it.resultados) }
                .onFailure { _state.value = UiState.Error(it.message ?: "Error desconocido") }
        }
    }
}

class IncidenciaViewModelFactory(
    private val repository: IncidenciaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidenciaViewModel::class.java)) {
            return IncidenciaViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}