// ===========================================================================
// VERSION TEMPORAL SIN PROTECCION - SOLO PARA EL EXPERIMENTO (Parte 3)
// ===========================================================================
// Fuera de src/: Gradle NO lo compila aqui.
//
// Variante del ViewModel de detalle para la misma prueba sin try/catch.
// ----------------------------------------------------------------------------

package com.ucaldas.rickmorty.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucaldas.rickmorty.data.Personaje
import com.ucaldas.rickmorty.data.PersonajesRepository
import com.ucaldas.rickmorty.data.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetalleUiState {
    data object Loading : DetalleUiState
    data class Success(val personaje: Personaje) : DetalleUiState
    data class Error(val message: String) : DetalleUiState
}

class DetalleViewModel(
    private val repository: PersonajesRepository =
        PersonajesRepository(RetrofitInstance.api)
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Loading)
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    fun cargar(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetalleUiState.Loading
            // SIN try/catch: la excepcion escapa al fallar la red.
            val personaje = repository.obtenerPersonaje(id)
            _uiState.value = DetalleUiState.Success(personaje)
        }
    }
}
