# 📄 Formato de archivos CSV

Flashes lee archivos `.csv` con un formato específico y sencillo.

## Estructura requerida

```csv
primera_columna,segunda_columna
Texto del frente,Texto del reverso
Otra pregunta,Otra respuesta
```

### Columnas obligatorias

| Nombre de columna | Descripción |
|---|---|
| `primera_columna` | Texto que aparece en el **frente** de la tarjeta (pregunta) |
| `segunda_columna` | Texto que aparece en el **reverso** de la tarjeta (respuesta) |
| `tercera_columna` | Indice de la palabra |

- Los nombres de columna son **insensibles a mayúsculas/minúsculas**.
- El orden de las columnas en el archivo no importa; se detectan por nombre.
- Las columnas adicionales son ignoradas.

### Índice de fila (`tercera_columna`)

La app asigna automáticamente un **índice** a cada tarjeta según su posición original en el CSV (comenzando en `1`). Este índice se guarda internamente como `tercera_columna` y no necesita ser escrito en el archivo.

> El índice refleja el orden de aparición en el CSV **antes** de cualquier mezcla aleatoria, por lo que permanece estable sin importar cómo se barajen las tarjetas.

## Ejemplo completo

```csv
primera_columna,segunda_columna, tercera_columna
Hola,Hello,1
Casa,House,2
Perro,Dog,3
"Buenas tardes, ¿cómo estás?","Good afternoon, how are you?",3
```

## Texto con comas

Si el contenido de una celda contiene comas, encerrarlo entre comillas dobles:

```csv
primera_columna,segunda_columna
"Rojo, azul y verde","Red, blue and green"
```

## Codificación

Los archivos deben estar codificados en **UTF-8** para soportar caracteres especiales (tildes, ñ, etc.).

## Dónde colocar los archivos

Los archivos `.csv` deben estar en:

```
myflashcardapp/Docs/Barajas/
```

Podés copiarlos manualmente ahí, o usar el botón **+** dentro de la pantalla **Barajas** para que la app los importe automáticamente. Por defecto vienen: **PruebaDoc** que contiene informacion de prueba de funcionamiento, **Investigacion**
, **Lenguaje**, **Geografia**, como trivias conjunto a 5 avatares.
