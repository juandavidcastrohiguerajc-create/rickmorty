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
 * Estados de la pantalla de lista: Loading, Success y Error.
 * La UI solo dibuja uno de estos tres, nunca un null intermedio.
 */
sealed interface UiState {
    data object Loading : UiState
    data class Success(val items: List<Personaje>) : UiState
    data class Error(val message: String) : UiState
}

/**
 * ViewModel de la lista de personajes.
 *
 * - El estado se expone como StateFlow y se colecciona desde Compose con
 *   collectAsStateWithLifecycle().
 * - cargar() puede invocarse tantas veces como haga falta (botón Recargar de
 *   Success y botón Reintentar de Error) sin abrir otra instancia.
 * - El Repository ya devuelve Result, por lo que la excepción NUNCA escapa de
 *   viewModelScope.launch y la app no puede crashear por un fallo de red.
 *
 * (Si se quisiera el bonus de mensajes distintos, aqui se inspeccionaria el
 * Throwable de onFailure: IOException = fallo de conexión, HttpException =
 * error HTTP del servidor. Ese análisis también está en ANALISIS.md.)
 */
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
            _uiState.value = repository.obtenerPersonajes().fold(
                onSuccess = { UiState.Success(it.results) },
                onFailure = { UiState.Error("No se pudo cargar. Reintenta.") }
            )
        }
    }
}
