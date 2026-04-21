# ▶ Play — Modo de estudio

La pantalla de **Play** presenta las tarjetas de la baraja activa en orden aleatorio.

## Requisito previo

Tener una baraja seleccionada en **Barajas**. Si no hay ninguna, aparece un aviso y la pantalla no se abre.

## Interfaz

```
┌──────────────────────────────────────────────────────┐
│  ⚡ PruebaDoc.csv       3 / 15            ✓  2 / 15  │
│──────────────────────────────────────────────────────│
│                                                      │
│                    PREGUNTA                          │
│                                                      │
│                      Casa                           │
│                                                      │
│                  clic para girar                     │
│──────────────────────────────────────────────────────│
│            [←]    [✓]    [✕]    [→]                 │
└──────────────────────────────────────────────────────┘
```

## Controles

| Control | Acción |
|---|---|
| **Clic en la tarjeta** | Gira la tarjeta mostrando PREGUNTA / RESPUESTA en loop |
| **→** | Avanza a la siguiente tarjeta |
| **←** | Retrocede a la tarjeta anterior (deshabilitado en la primera) |
| **✓** (verde) | Marca la tarjeta como correcta y avanza |
| **✕** (rojo) | Marca la tarjeta como incorrecta y avanza |

## Contador de aciertos

- Aparece en la esquina superior derecha: `✓ 2 / 15`
- Cuenta tarjetas **únicas** correctas — si marcás la misma tarjeta correcta varias veces, solo suma 1.
- Marcar una tarjeta como incorrecta (✕) la quita del conteo si estaba marcada.

## Tamaño de fuente adaptativo

El texto se ajusta automáticamente según su longitud:

| Longitud del texto | Tamaño de fuente |
|---|---|
| ≤ 20 caracteres | 32 pt |
| ≤ 50 caracteres | 24 pt |
| ≤ 100 caracteres | 18 pt |
| > 100 caracteres | 14 pt |

## Animación de flip

Al hacer clic en la tarjeta, se ejecuta una animación de escala horizontal (contracción → expansión) que simula el volteo físico de una tarjeta.
