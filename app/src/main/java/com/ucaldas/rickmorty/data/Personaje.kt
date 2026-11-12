package com.ucaldas.rickmorty.data

/**
 * Modelo minimo de un personaje de https://rickandmortyapi.com/api/character
 *
 * Los nombres coinciden 1:1 con las claves del JSON (id, name, status, species,
 * image), por eso NO se necesita @SerializedName. Solo seria obligatorio si
 * renombraramos el campo, por ejemplo:
 *
 *     @SerializedName("image") val imagenUrl: String
 *
 * OJO con Gson: aunque Kotlin declara los campos como no nulos, Gson construye
 * el objeto por reflection SIN ejecutar el constructor. Si una clave viniera
 * ausente del JSON, el campo quedaria en null y el fallo recien apareceria al
 * usarlo en la UI (no en la conversion). No se asume que Gson pone "".
 *
 * @param id       identificador unico; es el que viaja en la ruta character/{id}.
 * @param name     nombre del personaje (se muestra en la lista y el detalle).
 * @param status   Alive / Dead / unknown (el estado que pide la lista).
 * @param species  especie; se muestra en el detalle (opciones de evidencia).
 * @param image    URL del retrato. Se modela por completitud del contrato JSON;
 *                 esta version no lo dibuja (se decidio no usar bonus Coil).
 */
data class Personaje(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val image: String
)

/**
 * Respuesta de la raiz de /api/character.
 *
 * El JSON real ademas trae "info" (count, pages, next, prev), pero no lo
 * modelamos: Gson ignora las claves extra y aqui solo necesitamos results.
 * Si "results" faltara en la respuesta, Gson dejaria la lista en null pese a
 * estar declarada como no nula; por eso solo se llama a este dato dentro del
 * flujo de Result (nunca sin pasar antes por el Repository).
 */
data class PersonajesResponse(
    val results: List<Personaje>
)
