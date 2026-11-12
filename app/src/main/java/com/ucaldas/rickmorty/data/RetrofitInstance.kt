package com.ucaldas.rickmorty.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Punto unico de acceso al cliente Retrofit.
 *
 * Se usa `by lazy` para construir el cliente una sola vez (es pesado) y en el
 * primer uso real. La base URL DEBE terminar en "/" si los @GET son relativos;
 * si no, Retrofit lanza un error de configuracion al construirlo.
 */
object RetrofitInstance {

    val api: RickMortyApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://rickandmortyapi.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RickMortyApi::class.java)
    }
}
