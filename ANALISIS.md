# ANALISIS.md — Taller 8: de la Pokédex guiada a Rick & Morty

- **Asignatura:** Programación para Dispositivos Móviles — Universidad de Caldas
- **Modalidad:** individual
- **API obligatoria:** `https://rickandmortyapi.com/api/character`

> Orden de este documento: primero la predicción (escrita **antes** de mirar la
> solución y antes de correr el experimento), después lo observado, la causa,
> la corrección y las diferencias de diseño. La sección 3 se escribió con lo
> que realmente mostró el Huawei Y9 (Logcat y registro de crash del sistema);
> nada de lo que aparece ahí está inventado.

---

## 1. Predicción inicial (10 min, ANTES del experimento)

> **Pregunta del taller:** «Si el Repository llama a Retrofit sin `try/catch`
> desde `viewModelScope.launch`, y desactivo internet antes de volver a cargar,
> ¿qué ocurre y por qué?»

**Mi predicción:**

1. Al pulsar *Recargar* sin internet, OkHttp no podrá resolver el nombre
   `rickandmortyapi.com` (DNS) ni completar el TCP, así que
   `api.obtenerPersonajes()` lanzará una excepción de transporte de la familia
   `IOException`. Mi apuesta concreta es `UnknownHostException`; también podrían
   aparecer `ConnectException` o `SocketTimeoutException` según la red y el
   dispositivo, y eso solo lo decidirá la captura de Logcat.
2. Como el Repository **no** tiene `try/catch`, la excepción escapa hasta el
   bloque de `viewModelScope.launch`. Ese scope se construye con
   `SupervisorJob + Dispatchers.Main.immediate`, **pero sin
   `CoroutineExceptionHandler`**: el `SupervisorJob` evita que la falla cancele
   al scope y a sus hermanos, no "traga" la excepción.
3. Al no encontrar ningún manejador, la excepción termina en el manejador por
   defecto del hilo (`Thread.uncaughtExceptionHandler`), que en Android es el
   Runtime del sistema: Logcat mostrará `FATAL EXCEPTION: main` con la excepción
   exacta y **el proceso de la app morirá (crash)**. La UI nunca recibe estado
   alguno, así que no aparecerá la pantalla de Error.
4. Comportamiento alternativo que anoto por honestidad: si la excepción
   ocurriera fuera del hilo principal y existiera un `CoroutineExceptionHandler`
   en algún nivel, en vez de crashear la app podría quedar silenciosa y la
   pantalla se quedaría en `Loading` para siempre. En los dos casos el resultado
   es el mismo respecto al taller: **la versión sin protección nunca llega a
   pintar `UiState.Error`**.

---

## 2. Contrato del JSON: Rick & Morty vs PokéAPI

**URL explorada:** <https://rickandmortyapi.com/api/character>
(`info.count = 826`, `results` trae 20 objetos por página; un objeto real es
`{"id": 1, "name": "Rick Sanchez", "status": "Alive", "species": "Human", "type": "", "gender": "Male", "origin": {...}, "location": {...}, "image": "https://rickandmortyapi.com/api/character/avatar/1.jpeg", "episode": [...], "url": "...", "created": "..."}`)

Las dos respuestas comparten el envoltorio `results`, pero los objetos dentro
son distintos: en PokéAPI cada elemento de `results` es un recurso mínimo
(`name`, `url` con el id escondido en la URL) al que hay que pedir
`pokemon/{id}` para sus tipos e imágenes, mientras que en Rick & Morty cada
elemento de `results` ya es un personaje completo (`id`, `name`, `status`,
`species`, `image`, `origin`, `location`, `episode`) y su URL de detalle es
`character/{id}`.

---

## 3. Excepción observada (experimento de la Parte 3)

**Excepción observada (real): `java.net.UnknownHostException`** — se reprodujo
dos veces (20:46:51 y 20:53:58) con el mismo resultado. Traza completa del
registro de crash del sistema (`adb shell dumpsys dropbox --print`, entrada
`data_app_crash`; archivo `evidencias/03_logcat_excepcion.txt`):

```
java.net.UnknownHostException: Unable to resolve host "rickandmortyapi.com": No address associated with hostname
	at java.net.Inet6AddressImpl.lookupHostByName(Inet6AddressImpl.java:157)
	at java.net.Inet6AddressImpl.lookupAllHostAddr(Inet6AddressImpl.java:105)
	at java.net.InetAddress.getAllByName(InetAddress.java:1154)
	at okhttp3.Dns.lambda$static$0(Dns.java:39)
	at okhttp3.internal.connection.RouteSelector.resetNextInetSocketAddress(RouteSelector.java:171)
	...
	at okhttp3.RealCall$AsyncCall.execute(RealCall.java:172)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:641)
```

Y en Logcat (`adb logcat -v time -b crash,main,events`, captura en vivo):

```
E/AndroidRuntime(32325): FATAL EXCEPTION: main
E/AndroidRuntime(32325): Process: com.ucaldas.rickmorty, PID: 32325
I/am_crash( 1491): [32325,0,com.ucaldas.rickmorty,...,android.system.GaiException,android_getaddrinfo failed: EAI_NODATA (No address associated with hostname),Linux.java,-2]
I/Process (32325): Sending signal. PID: 32325 SIG: 9
```

- **¿Acertó la predicción?** Sí: familia `IOException` concreta
  `UnknownHostException` (§1, punto 1) y crash de la app sin pintar `Error`
  (§1, puntos 3–4). `am_crash` confirma la causa de fondo: el resolver DNS
  devolvió `EAI_NODATA` (sin red no hay respuesta del resolver).
- **Nota de observación sobre Logcat (EMUI 9, sin inventar):** el buffer de
  Logcat devolvió el encabezado `FATAL EXCEPTION: main`, `Process: ...` y el
  evento `am_crash` con la causa DNS, pero **no** volcó las líneas de la traza
  Java de la app (verificado con `adb logcat -d -b all` y con captura en vivo;
  otras apps sí muestran sus trazas en ese mismo buffer). Por eso la traza
  completa se recupera del registro de crash del sistema (`dumpsys dropbox`),
  que es la misma fuente que usa Android Studio para mostrar el stack en su
  pestaña de Logcat. Ambos volcados quedan juntos en `evidencias/03`.

**Qué se hizo para reproducirla:**

1. Instalar la versión temporal sin protección
   (`experimento/PersonajesRepository_sin_proteccion.kt` copiado sobre
   `data/PersonajesRepository.kt`).
2. Abrir la app **con internet** para tener la lista inicial.
3. Desactivar la conexión **efectiva** del dispositivo (Wi-Fi y datos móviles;
   apagar solo datos móviles no basta si el Wi-Fi sigue activo).
4. Pulsar *Recargar* (un GET nuevo sin cerrar la app) y capturar Logcat.

---

## 4. Causa técnica: por qué la corrutina falla y la app se cierra

`viewModelScope.launch { ... }` es una corrutina hija con `SupervisorJob`. Cuando
la excepción no está capturada dentro del `try` de la corrutina:

1. La corrutina hijo termina en estado fallido con esa excepción.
2. `SupervisorJob` **no** propaga la cancelación a los hermanos (por eso el
   scope sigue vivo), pero **tampoco instala ningún `CoroutineExceptionHandler`**
   que procese la falla.
3. La excepción se propaga por la jerarquía de contextos hasta que no encuentra
   manejador alguno y cae en el manejador por defecto del hilo.
4. En Android ese manejador final mata el proceso: Logcat registra
   `FATAL EXCEPTION` y la app se cierra. Es decir, **no haber capturado la
   excepción convierte un problema de red en un crash**, porque un fallo de
   transporte queda fuera del ciclo de vida que la UI entiende.

---

## 5. Corrección aplicada (versión final entregada)

El Repository de la entrega **sí** tiene `try/catch` y devuelve `Result`:

```kotlin
suspend fun obtenerPersonajes(): Result<PersonajesResponse> =
    try {
        Result.success(api.obtenerPersonajes())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
```

- `CancellationException` se **relanza**: la cancelación normal de la corrutina
  (salir de la pantalla, destruir el ViewModel) no es un error de UI y convertirla
  en `Error` rompería la estructura de corrutinas.
- Cualquier otra `Exception` queda en `Result.failure(...)`, y el ViewModel la
  transforma en `UiState.Error("No se pudo cargar. Reintenta.")` → la UI pinta
  el botón *Reintentar* → al pulsarlo, `cargar()` repite el GET y, con la red
  restaurada, vuelve a `UiState.Success`.
- En el código no se usa `!!` en ningún lugar: los datos se leen siempre dentro
  del estado que ya validó el `fold`.

---

## 6. Error HTTP vs fallo de conexión

| | **Error HTTP** | **Fallo de conexión** |
|---|---|---|
| ¿Hubo respuesta? | Sí: el servidor contestó con un código (404, 500…) | No: nunca llegó una respuesta |
| Excepción típica | `HttpException` (Retrofit la lanza con `code()` y `message()` del servidor) | `IOException` (`UnknownHostException`, `ConnectException`, `SocketTimeoutException`) |
| Dónde falla | Aplicación/servidor, después del HTTP | Transporte (DNS, TCP, TLS, timeout), antes del HTTP |
| Qué significa en esta app | La API cambió o el recurso no existe (p. ej. `character/999999`) | Sin internet, Wi-Fi apagado, DNS caído, timeout |
| Ejemplo en esta app | Abrir un id inexistente → 404 con respuesta | Cortar la red y pulsar *Recargar* |

Por eso **no** se atribuye `UnknownHostException` automáticamente a la falta del
permiso `INTERNET`: ese permiso ausente produce un fallo distinto (la petición
ni siquiera se emite), y `UnknownHostException` es un síntoma de resolución DNS
sin conectividad. La distinción real entre ambas familias solo se puede ver
inspeccionando el `Throwable` que devuelve `Result.failure` *(esta versión no
incluye bonus de mensajes diferenciados; el taller lo resuelve en el análisis,
no en la UI)*.

---

## 7. Decisión de diseño del detalle

**Opción elegida: (A) — ViewModel de detalle que consulta `character/{id}`.**

- La ruta de Navigation Compose es `detalle/{id}` y se envía **solo el id**
  (`NavType.IntType`), nunca el objeto completo.
- Cada entrada de detalle crea su `DetalleViewModel` (scoped al
  `NavBackStackEntry`), que llama a `repository.obtenerPersonaje(id)` dentro de
  `viewModelScope.launch` y expone sus propios `Loading / Success / Error` con
  `collectAsStateWithLifecycle()`.
- Ningún `Composable` hace peticiones HTTP: `LaunchedEffect(id)` solo le pide al
  ViewModel que cargue; Retrofit corre dentro de la corrutina del ViewModel.

**¿Qué pasaría si se abre directamente la ruta de detalle sin lista previa?**

Funciona: el detalle no depende de la lista, solo del `id` de la ruta y de la
red; consulta `character/{id}` y pinta el personaje. Si no hay red en ese
momento, el detalle muestra su **propio** estado `Error` con *Reintentar*
(independiente del de la lista). La única diferencia respecto a llegar desde la
lista es que no hay de dónde "recordar" datos ya cargados: al ser la opción A,
nunca se leen datos del ViewModel de la lista.

---

## 8. Lista de evidencias

| # | Evidencia | Archivo | Estado |
|---|-----------|---------|--------|
| 1 | URL explorada + JSON real (instancia de `Personaje`) | `evidencias/00_json_character.txt` | ✅ |
| 2 | Lista de la versión temporal con internet (previa al experimento) | `evidencias/00b_lista_version_temporal.png` | ✅ |
| 3 | Captura de la lista real con internet | `evidencias/01_lista.png` | ✅ |
| 4 | Captura del detalle al tocar una fila | `evidencias/02_detalle.png` | ✅ |
| 5 | Logcat de la versión **sin** manejo de errores (excepción exacta) | `evidencias/03_logcat_excepcion.txt` / `.png` | ✅ |
| 6 | Versión corregida en estado `Error` (sin crash) | `evidencias/04_error_corregido.png` | ✅ |
| 7 | Éxito tras recuperar la red y pulsar *Reintentar* | `evidencias/05_reintento_exitoso.png` | ✅ |

---

## 9. Checkpoints del taller

- **Checkpoint 1:** instancia real de `Personaje` identificada en el JSON
  (`{"id": 1, "name": "Rick Sanchez", ...}`), Gradle sincroniza y los modelos
  compilan. ✅ `evidencias/00_json_character.txt`
- **Checkpoint 2:** lista con personajes reales e internet + botón *Recargar*
  que dispara un nuevo GET. ✅ `evidencias/01_lista.png` y `00b_lista_version_temporal.png`
- **Checkpoint 3:** predicción escrita antes (§1), excepción observada
  `UnknownHostException` (§3) y Repository corregido con `Result` (§5): la
  petición desconectada produce un fallo manejable. ✅ `evidencias/03_*`
- **Checkpoint 4:** fila → detalle (§2/§7); red apagada → `Error` sin crash
  (§3/§5); red restaurada + *Reintentar* → `Success`. ✅ capturas
  `02`, `04` y `05` (antes/después).
