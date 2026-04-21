# Notas de mejora — Flashes

Registro de observaciones sobre el código actual. No son bugs, sino puntos
a considerar si el proyecto sigue creciendo.

---

## AppState.java

- **`randomizarBaraja` no se persiste.**  
  Se guarda `USUARIO` y `TAG` en `Usuario.txt`, pero el toggle de randomizar
  vuelve a `true` cada vez que se reinicia la app. Habría que agregar una línea
  `RANDOM=true/false` al archivo y leerla en `loadProfile()`.

- **`AVATAR_TAGS` duplicado.**  
  El arreglo `{"[GEO]", "[INV]", "[LEN]", "[MED]", "[SOF]"}` está definido
  idénticamente en `AppState` y en `ConfigView`. Si se agrega un avatar nuevo
  hay que acordarse de cambiarlo en dos lugares.

- **Rutas hardcodeadas en cada vista.**  
  `BARAJAS_DIR` vive en `BarajasView`, `LOGS_DIR` en `LogsView`. Tendría más
  sentido centralizar todas las rutas del proyecto en `AppState`, que ya maneja
  `USUARIO_FILE`.

---

## PlayView.java

- **`parseCsv` y `writeLog` son métodos de la vista.**  
  La lógica de parseo de CSV y escritura de logs no tiene nada que ver con
  dibujar la interfaz. Extraerlos a clases utilitarias (`CsvParser`, `LogWriter`)
  haría `PlayView` más corta y esas utilidades más fáciles de testear.

- **Timer hardcodeado a 12 ms.**  
  El intervalo de la animación de flip está "quemado" en el código. Una constante
  con nombre (`FLIP_INTERVAL_MS = 12`) comunicaría la intención y facilitaría
  ajustarlo.

- **No hay límite de tarjetas.**  
  Si un CSV tuviera miles de filas, toda la lista se carga en memoria de golpe.
  Para barajas grandes convendría paginar o cargar de forma lazy.

---

## ConfigView.java

- **`AVATAR_FILES` asume nombres exactos de archivo.**  
  Si los PNG de avatares no existen, `ImageIO.read()` lanza una excepción que se
  silencia con `ignored`. El botón queda vacío sin ningún aviso visual al usuario.
  Podría mostrarse un placeholder o un ícono genérico.

---

## BarajasView.java / MainMenu.java

- **Paleta de colores duplicada en cada vista.**  
  `BG`, `ACCENT`, `TEXT_MAIN`, etc. se repiten con los mismos valores en los seis
  archivos. Una clase `Theme` con constantes estáticas evitaría que un cambio de
  color requiera editar todos los archivos.

- **`CARD_BG` sin usar en `MainMenu`.**  
  El compilador ya avisa: la constante está declarada pero nunca referenciada.
  Se puede eliminar.

---

## General

- **No hay `.gitignore`.**  
  Logs personales, `Usuario.txt`, imágenes de avatares y el directorio `target/`
  de Maven se subirían a un repositorio público tal como está. Ver `Tree.txt`.

- **Sin tests.**  
  El directorio `src/test/java` existe pero está vacío. Al menos `parseCsv` y
  `AppState.loadProfile/saveProfile` serían buenos candidatos para pruebas unitarias.

- **`LogsView` no limita la cantidad de logs.**  
  Los archivos se acumulan indefinidamente en `Docs/Logs/`. Podría implementarse
  una política de retención (p. ej. conservar solo los últimos 30).
