# Taller 8 — De la Pokédex guiada a Rick & Morty

**Asignatura:** Programación para Dispositivos Móviles — Universidad de Caldas
**Tema:** Retrofit + Navigation Compose + estados de carga/error con manejo de excepciones

App Android (Kotlin + Jetpack Compose) que consume la API pública
[https://rickandmortyapi.com/api/character](https://rickandmortyapi.com/api/character):
lista de personajes, detalle por id y estados **Loading / Success / Error** con
botón **Reintentar**. Todo el código, la interfaz y esta documentación están en español.

## Qué contiene el taller

| Parte | Dónde está |
|-------|------------|
| Modelos + Retrofit + Gson | `app/src/main/java/com/ucaldas/rickmorty/data/` |
| Repository con `Result` (la corrección) | `data/PersonajesRepository.kt` |
| Estados de UI y ViewModels | `ui/PersonajesViewModel.kt`, `ui/DetalleViewModel.kt` |
| Pantallas Compose | `ui/PantallaLista.kt`, `ui/PantallaDetalle.kt` |
| Navegación lista → `detalle/{id}` | `MainActivity.kt` (`AppNavigation`) |
| Predicción, análisis y decisiones | `ANALISIS.md` |
| Versión temporal **sin** `try/catch` (solo experimento) | `experimento/*.kt` (fuera de `src/`, no compila) |
| Evidencias (capturas + Logcat) | `evidencias/` |

## Cómo ejecutarla

1. Abrir la carpeta en Android Studio (o usar `gradlew.bat assembleDebug`).
2. Conectar el dispositivo con **depuración USB** activada y tocar *Run*.
3. Requisitos: `minSdk 28`, `compileSdk 37`, Retrofit/Converter-Gson **2.11.0**,
   Navigation Compose 2.8.0, Lifecycle **2.6.1**, Compose BOM.

## Cómo reproducir el experimento de red (Parte 3)

> ⚠️ Solo con la versión temporal de `experimento/`. **Antes de entregar,
> restaurar los tres archivos `*_corregida_final.kt` y recompilar.**

1. Copiar sobre `src/`: `PersonajesRepository_sin_proteccion.kt`,
   `PersonajesViewModel_sin_proteccion.kt`, `DetalleViewModel_sin_proteccion.kt`.
2. Compilar, instalar y abrir la app **con internet** (la lista debe cargar).
3. Cortar la red efectiva del teléfono:
   `adb shell svc wifi disable` + `adb shell svc data disable`
   (verificar con `adb shell dumpsys connectivity` → `Active default network: none`).
4. Pulsar **Recargar** y capturar Logcat:
   `adb logcat -v time -b crash,main,events`.
5. Resultado observado (Huawei Y9, 2026-11-05):
   `java.net.UnknownHostException: Unable to resolve host "rickandmortyapi.com"`
   → `FATAL EXCEPTION: main` → la app se cierra sola.
6. Restaurar la versión corregida, recompilar y repetir: ahora aparece el
   estado **Error** con **Reintentar** y, con la red de vuelta, **Success**.

## Evidencias

| Archivo | Qué muestra |
|---------|-------------|
| `evidencias/00_json_character.txt` | JSON real de la API (Checkpoint 1) |
| `evidencias/00b_lista_version_temporal.png` | Lista con la versión temporal (previa al experimento) |
| `evidencias/01_lista.png` | Lista real con internet |
| `evidencias/02_detalle.png` | Detalle al tocar una fila (Rick Sanchez, id 1) |
| `evidencias/03_logcat_excepcion.txt` / `.png` | Logcat del crash **sin** manejo de errores + traza completa |
| `evidencias/04_error_corregido.png` | Versión corregida en estado Error (sin crash) |
| `evidencias/05_reintento_exitoso.png` | Éxito tras pulsar Reintentar con la red restaurada |

## Decisiones y análisis

El análisis completo —predicción escrita **antes** del experimento, diferencia
del JSON frente a PokéAPI, causa técnica de la corrutina (`SupervisorJob` sin
`CoroutineExceptionHandler`), diferencia `IOException` vs `HttpException` y la
opción de diseño elegida para el detalle (opción A)— está en **[`ANALISIS.md`](ANALISIS.md)**.

**Sin bonus (decisión del taller):** no usa Coil ni paginación, y no muestra
mensajes diferenciados de `IOException`/`HttpException` en la UI: esa
distinción se explica en `ANALISIS.md` §6.

## Lista de verificación final

- [x] Permiso `INTERNET` **fuera** de `<application>` (`AndroidManifest.xml`)
- [x] Base URL termina en `/` (`https://rickandmortyapi.com/api/`)
- [x] Claves del JSON coinciden con los modelos (sin `@SerializedName` necesario)
- [x] Llamadas `suspend` en `RickMortyApi`, invocadas desde `viewModelScope.launch`
- [x] `CancellationException` **relanzada** en el Repository
- [x] Sin `!!` en todo el código
- [x] Estados Loading / Success / Error con botón **Reintentar**
- [x] Detalle navegable por `detalle/{id}` (solo se envía el id)
- [x] Evidencia antes/después del experimento (`evidencias/03` → `04` → `05`)
