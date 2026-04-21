# 🗂 Barajas — Gestión de barajas

La pantalla de **Barajas** es el administrador de tus archivos de estudio.

## Acceso

Menú principal → botón **Barajas**.

## Interfaz

```
┌─────────────────────────────────────────┐
│  🗂 Barajas                          [+] │
│─────────────────────────────────────────│
│  ┌───────────┐  ┌───────────┐           │
│  │ 📋        │  │ 📋        │           │
│  │ deck1.csv │  │ deck2.csv │           │
│  │ 2 KB  ✓ ✕│  │ 5 KB  ✓ ✕│           │
│  └───────────┘  └───────────┘           │
│─────────────────────────────────────────│
│  Baraja activa: deck1.csv         [OK]  │
└─────────────────────────────────────────┘
```

## Acciones

| Elemento | Acción |
|---|---|
| **+** (esquina superior derecha) | Abre selector de archivos; copia el `.csv` elegido a `Docs/Barajas/` |
| **✓** (verde, en cada tarjeta) | Selecciona esa baraja como activa para toda la sesión |
| **✕** (rojo, en cada tarjeta) | Elimina el archivo de `Docs/Barajas/` (pide confirmación) |
| **OK** (esquina inferior derecha) | Cierra la ventana |

## Reglas de seguridad

- Solo se aceptan archivos con extensión `.csv`.
- Los archivos se copian dentro de `Docs/Barajas/`; no se puede apuntar a rutas externas.
- Si se elimina la baraja activa, el estado global pasa a "No hay barajas seleccionadas".

## Estado de selección

La barra inferior muestra en todo momento:
- **Violeta** — nombre de la baraja activa.
- **Gris** — "No hay barajas seleccionadas" si no hay ninguna elegida.
