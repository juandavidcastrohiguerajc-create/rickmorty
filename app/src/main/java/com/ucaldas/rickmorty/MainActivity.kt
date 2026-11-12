package com.ucaldas.rickmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ucaldas.rickmorty.ui.DetalleViewModel
import com.ucaldas.rickmorty.ui.PantallaDetalle
import com.ucaldas.rickmorty.ui.PantallaLista
import com.ucaldas.rickmorty.ui.PersonajesViewModel
import com.ucaldas.rickmorty.ui.theme.RickMortyTheme

/**
 * Rutas de Navigation Compose.
 *
 * Se navega enviando SOLO el id (entero), nunca el objeto completo: la ruta es
 * "detalle/{id}" y al abrirla el DetalleViewModel consulta character/{id}.
 */
object Rutas {
    const val LISTA = "lista"
    const val DETALLE = "detalle/{id}"
    fun detalle(id: Int) = "detalle/$id"
}

/**
 * Conecta lista y detalle con NavHost.
 *
 * En Compose la navegación no mueve pantallas físicas: se decide qué composable
 * se dibuja según la ruta activa.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ViewModel de la lista a nivel de Activity: sobrevive a los cambios de
    // configuración y a los viajes al detalle (no se recarga al volver).
    val personajesViewModel: PersonajesViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Rutas.LISTA
    ) {

        composable(Rutas.LISTA) {
            val uiState by personajesViewModel.uiState.collectAsStateWithLifecycle()

            PantallaLista(
                uiState = uiState,
                onReintentar = personajesViewModel::cargar,
                onPersonajeClick = { id ->
                    // launchSingleTop evita apilar copias del mismo detalle
                    // si el usuario pulsa la fila varias veces rápido.
                    navController.navigate(Rutas.detalle(id)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("id") ?: return@composable

            // OPCIÓN A: ViewModel propio del detalle, instanciado por cada
            // entrada de la ruta (scoped al NavBackStackEntry).
            val detalleViewModel: DetalleViewModel = viewModel()

            // Primera carga de character/{id}. El Composable NO hace peticiones
            // HTTP: solo le pide al ViewModel que cargue; Retrofit corre dentro
            // de viewModelScope.launch.
            LaunchedEffect(id) {
                detalleViewModel.cargar(id)
            }

            val uiState by detalleViewModel.uiState.collectAsStateWithLifecycle()

            PantallaDetalle(
                uiState = uiState,
                onReintentar = { detalleViewModel.cargar(id) },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RickMortyTheme {
                AppNavigation()
            }
        }
    }
}
