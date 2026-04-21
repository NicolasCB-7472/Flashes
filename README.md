# ⚡ Flashes

Aplicación de escritorio para el estudio con tarjetas de memoria (*flashcards*), construida en **Java 21** con **Java Swing**.

---

## Características

- **Barajas CSV** — importá cualquier archivo `.csv` con columnas `primera_columna` / `segunda_columna`.
- **Modo Play** — las tarjetas se presentan en orden aleatorio con animación de flip horizontal.
- **Seguimiento de aciertos** — marcá cada tarjeta como correcta (✓) o incorrecta (✕); el contador de únicas correctas se actualiza en tiempo real.
- **Navegación libre** — avanzá (→) o retrocedé (←) entre tarjetas en cualquier momento.
- **Texto adaptativo** — el tamaño de fuente de cada tarjeta se ajusta automáticamente según la longitud del contenido.
- **Selección de baraja global** — la baraja activa se persiste en memoria durante toda la sesión y se muestra en la pantalla de Barajas.

---

## Requisitos

| Herramienta | Versión mínima |
|---|---|
| Java (JDK) | 21 |
| Apache Maven | 3.6+ |

---

## Estructura del proyecto

```
myflashcardapp/
├── Docs/
│   ├── Barajas/          ← Directorio de barajas (.csv)
│   │   └── PruebaDoc.csv
│   └── Help/
│       ├── barajas.md
│       ├── play.md
│       └── formato-csv.md
├── src/
│   └── main/java/com/flashcards/
│       ├── Main.java          ← Punto de entrada
│       ├── MainMenu.java      ← Menú principal
│       ├── BarajasView.java   ← Gestión de barajas
│       ├── PlayView.java      ← Modo de estudio
│       └── AppState.java      ← Estado global de sesión
└── pom.xml
```

---

## Compilar y ejecutar

```bash
cd myflashcardapp
mvn compile
mvn exec:java -Dexec.mainClass="com.flashcards.Main"
```

O usando el runner de VS Code / IntelliJ directamente sobre `Main.java`.

---

## Formato de barajas

Los archivos `.csv` deben tener como mínimo estas dos columnas (el orden no importa):

```csv
primera_columna,segunda_columna
Hola,Hello
Casa,House
```

- `primera_columna` → lado frontal de la tarjeta (pregunta)
- `segunda_columna` → lado trasero de la tarjeta (respuesta)

Se soportan valores entre comillas dobles para texto con comas.

---

## Cómo agregar una baraja

1. Abrí **Barajas** desde el menú principal.
2. Hacé click en el botón **+** (esquina superior derecha).
3. Seleccioná un archivo `.csv` válido desde tu sistema.
4. El archivo se copia automáticamente a `Docs/Barajas/`.
5. Hacé click en **✓** sobre la baraja para seleccionarla como activa.
6. Cerrá la ventana con **OK**.

---

## Licencia

Proyecto personal de uso libre.
