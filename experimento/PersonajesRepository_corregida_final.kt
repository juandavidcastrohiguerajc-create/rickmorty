package com.ucaldas.rickmorty.data

import kotlinx.coroutines.CancellationException

/**
 * Repositorio de personajes: unica puerta entre la app y la API.
 *
 * Version CORREGIDA (la entrega final). Devuelve Result para que el ViewModel
 * pueda pintar Success o Error sin que la excepcion escape de la corrutina.
 *
 * Reglas del try/catch:
 *  - CancellationException se RELANZA: la cancelacion normal de la corrutina
 *    (p. ej. al destruir la pantalla) no es un error de UI.
 *  - Cualquier otra Exception (IOException de red, HttpException de servidor)
 *    se envuelve en Result.failure para que el ViewModel lo transforme en
 *    UiState.Error.
 *
 * NOTA: para el experimento de la Parte 3 existio una version temporal SIN
 * try/catch, cuyo codigo se conserva en experimento/PersonajesRepository_
 * sin_proteccion.kt (fuera de src/, no compila) y se describe en ANALISIS.md.
 */
class PersonajesRepository(private val api: RickMortyApi) {

    /** GET /api/character -> lista completa de la primera pagina. */
    suspend fun obtenerPersonajes(): Result<PersonajesResponse> =
        try {
            Result.success(api.obtenerPersonajes())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    /** GET /api/character/{id} -> detalle de un personaje. */
    suspend fun obtenerPersonaje(id: Int): Result<Personaje> =
        try {
            Result.success(api.obtenerPersonaje(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
