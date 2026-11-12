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

/**
 * Estados de la pantalla de detalle: Loading, Success y Error.
 */
sealed interface DetalleUiState {
    data object Loading : DetalleUiState
    data class Success(val personaje: Personaje) : DetalleUiState
    data class Error(val message: String) : DetalleUiState
}

/**
 * ViewModel de la pantalla de detalle (OPCIÓN A del taller).
 *
 * Cada entrada de detalle crea su propia instancia (scoped al NavBackStackEntry)
 * y consulta GET /api/character/{id} con el id que llega por la ruta.
 * De esta forma el detalle funciona aunque se abra la ruta sin lista previa:
 * solo depende del id y de la red. Si no hay red, su estado Error ofrece
 * Reintentar de forma independiente al de la lista.
 */
class DetalleViewModel(
    private val repository: PersonajesRepository =
        PersonajesRepository(RetrofitInstance.api)
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleUiState>(DetalleUiState.Loading)
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    /**
     * Consulta character/{id}. Se llama desde LaunchedEffect(id) al entrar a la
     * ruta y desde el botón Reintentar del detalle. La petición HTTP NO se hace
     * desde el Composable: el Composable solo le pide al ViewModel que cargue.
     */
    fun cargar(id: Int) {
        viewModelScope.launch {
            _uiState.value = DetalleUiState.Loading
            _uiState.value = repository.obtenerPersonaje(id).fold(
                onSuccess = { DetalleUiState.Success(it) },
                onFailure = { DetalleUiState.Error("No se pudo cargar el detalle. Reintenta.") }
            )
        }
    }
}
