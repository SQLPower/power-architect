# PLAN.md — Plan de ejecución Power*Architect (modernización)

> Archivo de plan de alto nivel. El detalle atómico vive en `TAREAS.md`.
> El contexto acumulado (decisiones, specs, hallazgos) vive en `CONTEXTO.md`.
> Las funcionalidades aún no planificadas viven en `caracteristicas_futuras.md`.
> Preguntas abiertas al usuario viven en `preguntas.md`.

## Objetivo

Modernizar Power*Architect (SQL Power Architect) sobre la versión actual del repo:
- JDK 21 y dependencias/frameworks actualizados (Gradle; Spring mínimo).
- Zoom de aplicación: Ctrl+rueda y teclado +/- para el canvas, más factor global de UI (incluye fuentes).
- Modo oscuro y estilo visual moderno tipo DbSchema.
- Crear tabla desde DDL dado.
- Copiar el DDL individual de cada tabla/objeto sin exportar todo.
- Comentarios/notas en línea por tabla, validando que no rompan el SQL.

## Estado global

| Fase | Descripción | Estado |
|------|-------------|--------|
| 0 | Persistencia y saneamiento | HECHA |
| 1 | Cerrar JDK 21 + Gradle y modernizar dependencias | HECHA |
| 2 | Zoom (canvas + escala global UI) | HECHA |
| 3 | Modo oscuro + FlatLaf | HECHA |
| 4 | DDL (copiar por objeto, crear desde DDL, notas) | EN_PROGRESO |
| 5 | Estilo DbSchema (tablas + diálogos) | HECHA |
| 6 | Verificación (tests + checklist manual) | HECHA |

Leyenda de estados: `PENDIENTE`, `EN_PROGRESO`, `BLOQUEADA`, `HECHA`, `CANCELADA`.

## Fases

### Fase 0 — Persistencia y saneamiento
- Crear `PLAN.md`, `TAREAS.md`, `CONTEXTO.md`, `caracteristicas_futuras.md`.
- Limpiar artefactos generados en la raíz (`app-test*.log`, `TestingJob.kjb`, `testKettle*`).
- Actualizar `AGENTS.md` a Gradle + JDK 21.
- Commit.

### Fase 1 — Cerrar JDK 21 + Gradle y modernizar dependencias (prerequisito)
- Corregir classpath de clases generadas (annotation processor) para runtime de test.
- Dejar `gradlew test` verde o con fallos acotados y documentados.
- Migrar librerías vendidas a coordenadas Maven (con excepciones documentadas).
- Migrar superficie `org.springframework.security.*` a API propia.
- Corregir APIs eliminadas en JDK 21; añadir `--add-opens` si aplica.
- Marcar `build.xml` como deprecado.
- Commit.

### Fase 2 — Zoom
- Ctrl+rueda → zoom del canvas (anclado al cursor), conservando acciones actuales.
- Teclado numérico `+` / `-` → zoom del canvas.
- Factor global de UI (fuentes, diálogos, árbol) con nuevos iconos y menú.
- Persistencia de ambos factores.
- Commit.

### Fase 3 — Modo oscuro + FlatLaf
- FlatLaf como L&F; selector Claro/Oscuro/Sistema persistido.
- Paleta de tema compartida para componentes custom-painted (`PlayPen`, `TablePaneUI`, `RelationshipUI`).
- Commit.

### Fase 4 — DDL
- 4a: Copiar DDL por objeto (menú contextual + diálogo de preview).
- 4b: Parser `CREATE TABLE` multi-dialecto + diálogo "Crear tabla desde DDL".
- 4c: Notas por tabla anexadas al DDL con validación.
- Commit por sub-fase.

### Fase 5 — Estilo DbSchema
- Tarjetas de tabla modernas (header, esquinas, sombra, filas alternas, iconos PK/FK/NN).
- Diálogo de columnas limpio (tabs, búsqueda de tipos, validación inline).
- Commit.

### Fase 6 — Verificación
- Tests unitarios nuevos + suite existente.
- Checklist manual de UI.
- Commit final.

## Notas de ejecución
- Commits después de cada fase (decisión del usuario).
- Actualizar `TAREAS.md`, `CONTEXTO.md` y `PLAN.md` a medida que avanza cada fase.
