package com.ucaldas.rickmorty.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ucaldas.rickmorty.data.Personaje
import com.ucaldas.rickmorty.ui.theme.GrisDesconocido
import com.ucaldas.rickmorty.ui.theme.RojoMuerte
import com.ucaldas.rickmorty.ui.theme.VerdePortal

/**
 * Pantalla de lista: dibuja SOLO el estado que le llega.
 *
 * @param uiState          Loading / Success / Error del PersonajesViewModel.
 * @param onReintentar     invoca PersonajesViewModel.cargar(). Se conecta tanto
 *                         al botón Recargar (dentro de Success, para repetir la
 *                         prueba de red sin cerrar la app) como al botón
 *                         Reintentar del estado Error.
 * @param onPersonajeClick entrega el id de la fila pulsada para navegar al detalle.
 */
@Composable
fun PantallaLista(
    uiState: UiState,
    onReintentar: () -> Unit,
    onPersonajeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.message,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }
        }

        is UiState.Success -> {
            ListaPersonajes(
                personajes = uiState.items,
                onRecargar = onReintentar,
                onPersonajeClick = onPersonajeClick,
                modifier = modifier
            )
        }
    }
}

/**
 * Lista visible cuando todo salió bien.
 *
 * El botón Recargar permanece en pantalla aunque ya haya datos: es lo que
 * permite provocar un nuevo GET sin cerrar la app (experimento de red).
 */
@Composable
private fun ListaPersonajes(
    personajes: List<Personaje>,
    onRecargar: () -> Unit,
    onPersonajeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Personajes de Rick & Morty",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "${personajes.size} cargados desde rickandmortyapi.com",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(onClick = onRecargar) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Recargar")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(personajes, key = { it.id }) { personaje ->
                FilaPersonaje(
                    personaje = personaje,
                    onClick = { onPersonajeClick(personaje.id) }
                )
            }
        }
    }
}

/**
 * Fila clicable: nombre + estado (lo mínimo que pide el taller).
 * El color del punto y del texto sigue el estado real del JSON.
 */
@Composable
private fun FilaPersonaje(personaje: Personaje, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Punto de color: verde = Alive, rojo = Dead, gris = unknown.
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(colorDeEstado(personaje.status))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = personaje.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = personaje.status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorDeEstado(personaje.status)
                )
            }
        }
    }
}

/**
 * Color asociado al estado del personaje. Se comparte con el detalle.
 * (Los valores llegan del JSON: "Alive", "Dead" o "unknown".)
 */
@Composable
fun colorDeEstado(status: String): Color = when (status.lowercase()) {
    "alive" -> VerdePortal
    "dead" -> RojoMuerte
    else -> GrisDesconocido
}
