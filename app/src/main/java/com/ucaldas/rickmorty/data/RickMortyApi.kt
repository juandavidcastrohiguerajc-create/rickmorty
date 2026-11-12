package com.ucaldas.rickmorty.data

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Contrato HTTP con https://rickandmortyapi.com/api/
 *
 * La base URL del cliente termina en "/" y las anotaciones @GET son relativas:
 * "character" resuelve a /api/character y "character/{id}" a /api/character/1.
 * Las funciones son suspend para llamarse desde viewModelScope.launch.
 */
interface RickMortyApi {

    /** Lista de personajes: pagina 1 con 20 resultados. */
    @GET("character")
    suspend fun obtenerPersonajes(): PersonajesResponse

    /** Detalle: consulta el servidor con character/{id}. */
    @GET("character/{id}")
    suspend fun obtenerPersonaje(@Path("id") id: Int): Personaje
}
