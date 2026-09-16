# TAREAS.md — Tareas atómicas y su estado

> Estados: `PENDIENTE` | `EN_PROGRESO` | `BLOQUEADA` | `HECHA` | `CANCELADA`.
> Al retomar el trabajo, continuar desde la primera tarea no-`HECHA` de la fase `EN_PROGRESO`.

## Fase 0 — Persistencia y saneamiento

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 0.1 | Crear `PLAN.md` | HECHA | |
| 0.2 | Crear `TAREAS.md` | HECHA | |
| 0.3 | Crear `CONTEXTO.md` | HECHA | |
| 0.4 | Crear `caracteristicas_futuras.md` | HECHA | Plantilla con sugerencias de mercado |
| 0.5 | Limpiar artefactos de la raíz | HECHA | `app-test*.log`, `TestingJob.kjb`, `testKettle*` |
| 0.6 | Actualizar `AGENTS.md` a Gradle/JDK 21 | HECHA | |
| 0.7 | Commit Fase 0 | EN_PROGRESO | |

## Fase 1 — JDK 21 + Gradle + dependencias

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 1.1 | Diagnosticar fallo `ClassNotFoundException` en tests | PENDIENTE | `SPSessionPersister`/`SPPersisterHelper` generados |
| 1.2 | Corregir classpath de clases generadas (app + library) en test runtime | PENDIENTE | `build.gradle` sin commitear ya lo intenta |
| 1.3 | `gradlew test` verde o fallos acotados/documentados | PENDIENTE | Suite Swing es flaky (conocido) |
| 1.4 | Inventario de JARs vendidos y mapeo a coordenadas Maven | PENDIENTE | guava, log4j, httpclient, commons-*, junit, miglayout, rsyntaxtextarea |
| 1.5 | Migrar librerías con equivalente Maven | PENDIENTE | Dejar excepciones vendidas documentadas |
| 1.6 | Migrar superficie `org.springframework.security.*` a API propia | PENDIENTE | ~10 imports en `enterprise/` |
| 1.7 | Retirar `spring-core` y `spring-security-core` vendidos | PENDIENTE | |
| 1.8 | Corregir APIs eliminadas en JDK 21 + `--add-opens` | PENDIENTE | reflexión del persister/AP |
| 1.9 | Marcar `build.xml` deprecado / portar targets necesarios | PENDIENTE | conservar archivo |
| 1.10 | Commit Fase 1 | PENDIENTE | |

## Fase 2 — Zoom

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 2.1 | `MouseWheelListener` Ctrl+rueda → `PlayPen.setZoom` anclado al cursor | PENDIENTE | |
| 2.2 | Teclado numérico `+` / `-` → zoom del canvas | PENDIENTE | conservar acciones actuales |
| 2.3 | `UIScaleManager` con factor global persistido | PENDIENTE | reescala fuentes `UIManager` |
| 2.4 | Reaplicar escala a todas las ventanas (`updateComponentTreeUI`) | PENDIENTE | |
| 2.5 | Nuevos iconos + menú de zoom (subir/bajar/reset UI y canvas) | PENDIENTE | |
| 2.6 | Commit Fase 2 | PENDIENTE | |

## Fase 3 — Modo oscuro + FlatLaf

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 3.1 | Añadir dependencia FlatLaf y fijarla como L&F por defecto | PENDIENTE | `com.formdev:flatlaf` |
| 3.2 | Selector Claro/Oscuro/Sistema persistido | PENDIENTE | |
| 3.3 | Paleta de tema compartida | PENDIENTE | `ThemePalette` |
| 3.4 | Theming de `PlayPen`, `TablePaneUI`, `RelationshipUI` | PENDIENTE | |
| 3.5 | Commit Fase 3 | PENDIENTE | |

## Fase 4 — DDL

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 4a.1 | Acción `CopyDDLAction` en popup de `TablePane` y `DBTree` | PENDIENTE | |
| 4a.2 | Diálogo preview DDL con Copiar/Guardar | PENDIENTE | reutilizar `SQLScriptDialog` |
| 4a.3 | Generar DDL del objeto con índices/FKs/comentarios relacionados | PENDIENTE | |
| 4a.4 | Commit Fase 4a | PENDIENTE | |
| 4b.1 | `ddl/DDLParser.java` (tokenizer + recursive descent) | PENDIENTE | multi-dialecto |
| 4b.2 | Diálogo "Crear tabla desde DDL" (dialecto + preview + crear) | PENDIENTE | |
| 4b.3 | Acción de menú + manejo de errores con línea/columna | PENDIENTE | sin tablas parciales |
| 4b.4 | Tests `DDLParserTest` | PENDIENTE | |
| 4b.5 | Commit Fase 4b | PENDIENTE | |
| 4c.1 | Campo de notas por tabla (propiedad de `SQLObject`) | PENDIENTE | sin tocar annotation processor |
| 4c.2 | UI de notas en Table Properties y diálogo de copia | PENDIENTE | |
| 4c.3 | Anexar notas al DDL con validación/sanitización | PENDIENTE | |
| 4c.4 | Tests de validación de notas | PENDIENTE | |
| 4c.5 | Commit Fase 4c | PENDIENTE | |

## Fase 5 — Estilo DbSchema

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 5.1 | Reescritura `BasicTablePaneUI` (tarjeta moderna) | PENDIENTE | |
| 5.2 | Iconos PK/FK/NN y filas alternas | PENDIENTE | |
| 5.3 | Modernizar `ColumnEditPanel` (tabs, búsqueda tipos, validación) | PENDIENTE | |
| 5.4 | Commit Fase 5 | PENDIENTE | |

## Fase 6 — Verificación

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 6.1 | Tests unitarios nuevos | PENDIENTE | |
| 6.2 | `gradlew test` + documentar fallos flaky | PENDIENTE | |
| 6.3 | Checklist manual de UI | PENDIENTE | |
| 6.4 | Commit final | PENDIENTE | |
