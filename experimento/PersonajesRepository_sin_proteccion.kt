// ===========================================================================
// VERSION TEMPORAL SIN PROTECCION - SOLO PARA EL EXPERIMENTO (Parte 3)
// ===========================================================================
//
// Este archivo vive en experimento/, FUERA de src/, por lo que Gradle no lo
// compila. Se conserva como evidencia del codigo que se uso para provocar y
// observar el crash en Logcat.
//
// CÓMO REPRODUCIR EL EXPERIMENTO (y después RESTAURAR la versión corregida):
//
// 1. Copiar este contenido sobre
//    app/src/main/java/com/ucaldas/rickmorty/data/PersonajesRepository.kt
//    (reemplazando la versión corregida).
// 2. Compilar e instalar en el dispositivo CON internet y cargar la lista.
// 3. Desactivar la conexión efectiva del dispositivo (Wi-Fi y datos).
// 4. Pulsar "Recargar" en la app y capturar Logcat: se verá la excepción
//    exacta (p. ej. UnknownHostException) y el cierre de la app.
// 5. OBLIGATORIO antes de entregar: volver a copiar la versión corregida de
//    PersonajesRepository.kt (la que está en src/ y devuelve Result) y
//    recompilar.
//
// ----------------------------------------------------------------------------

package com.ucaldas.rickmorty.data

// IMPORTANTE: sin try/catch ni Result. La excepción de Retrofit escapa al
// viewModelScope.launch del ViewModel.
class PersonajesRepository(private val api: RickMortyApi) {

    // SOLO para el experimento: la excepción escapa al ViewModel.
    suspend fun obtenerPersonajes(): PersonajesResponse = api.obtenerPersonajes()

    // Variante del detalle usada en la misma prueba (también sin protección).
    suspend fun obtenerPersonaje(id: Int): Personaje = api.obtenerPersonaje(id)
}
