// ===========================================================================
// VERSION TEMPORAL SIN PROTECCION - SOLO PARA EL EXPERIMENTO (Parte 3)
// ===========================================================================
// Fuera de src/: Gradle NO lo compila aqui.
//
// Variante del ViewModel usada junto con
// PersonajesRepository_sin_proteccion.kt: como el Repository devuelve la
// respuesta directamente (sin Result), el ViewModel solo puede publicar
// Success cuando todo sale bien; NO existe estado Error controlado.
// Si el Repository lanza la excepcion, esta escapa de viewModelScope.launch.
//
// Para restaurar la version final: copiar los archivos *_corregida_final.kt
// de esta carpeta de vuelta sobre src/ y recompilar.
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

sealed interface UiState {
    data object Loading : UiState
    data class Success(val items: List<Personaje>) : UiState
    data class Error(val message: String) : UiState
}

class PersonajesViewModel(
    private val repository: PersonajesRepository =
        PersonajesRepository(RetrofitInstance.api)
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // SIN try/catch: si la red esta caida, la excepcion de Retrofit
            // (p. ej. UnknownHostException) escapa de aqui y la corrutina falla.
            val respuesta = repository.obtenerPersonajes()
            _uiState.value = UiState.Success(respuesta.results)
        }
    }
}
