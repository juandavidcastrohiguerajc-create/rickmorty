package com.ucaldas.rickmorty.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = VerdePortal,
    onPrimary = Color.White,
    primaryContainer = VerdePortalClaro,
    onPrimaryContainer = TextoOscuro,
    secondary = MoradoCielo,
    secondaryContainer = MoradoCieloClaro,
    onSecondaryContainer = TextoOscuro,
    background = FondoClaro,
    surface = FondoClaro,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdePortal,
    onPrimary = Color(0xFF052E16),
    secondary = MoradoCieloClaro,
    secondaryContainer = MoradoCielo,
    onSecondaryContainer = Color.White,
    background = FondoOscuro,
    surface = FondoOscuro,
    onBackground = TextoClaro,
    onSurface = TextoClaro
)

/**
 * Tema global de la app: colores y tipografia.
 * Se aplica una unica vez en MainActivity, dentro de setContent { }.
 */
@Composable
fun RickMortyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        content = content
    )
}
