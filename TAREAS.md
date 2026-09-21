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
| 0.7 | Commit Fase 0 | HECHA | `1f370cc1` |

## Fase 1 — JDK 21 + Gradle + dependencias

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 1.1 | Diagnosticar fallo `ClassNotFoundException` en tests | HECHA | Causa: clases generadas no estaban en runtime de test |
| 1.2 | Corregir classpath de clases generadas (app + library) en test runtime | HECHA | `build.gradle` (cambios previos + test classpath) |
| 1.3 | `gradlew test` verde o fallos acotados/documentados | HECHA | App: 1159 tests, 0 fallos. Librería: 2 fallos pre-existentes documentados |
| 1.4 | Inventario de JARs vendidos y mapeo a coordenadas Maven | HECHA | ver CONTEXTO §8 |
| 1.5 | Migrar librerías con equivalente Maven | HECHA | guava33, reload4j, codec, io, logging, httpclient4.x, jsr305; beanutils/collections quedan vendidos (regresión de tests al migrarlos) |
| 1.6 | Migrar superficie `org.springframework.security.*` a API propia | HECHA | interfaces locales `GrantedAuthority`/`UserDetails` + `AccessDeniedException` |
| 1.7 | Retirar `spring-core` y `spring-security-core` vendidos | HECHA | security retirado; `spring-core` se conserva (Kettle 3.2.2 lo requiere) |
| 1.8 | Corregir APIs eliminadas en JDK 21 + `--add-opens` | HECHA | compila en JDK21; solo warnings de `new Integer()` etc. |
| 1.9 | Marcar `build.xml` deprecado / portar targets necesarios | HECHA | nota de deprecación al inicio de `build.xml` |
| 1.10 | Commit Fase 1 | HECHA | ver CONTEXTO §8 |

## Fase 2 — Zoom

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 2.1 | `MouseWheelListener` Ctrl+rueda → `PlayPen.setZoom` anclado al cursor | HECHA | `ZoomOnWheelListener` en `PlayPen` |
| 2.2 | Teclado numérico `+` / `-` → zoom del canvas | HECHA | bindings `VK_ADD`/`VK_SUBTRACT` en `ArchitectFrame`; conserva Ctrl+Shift+= y Ctrl+- |
| 2.3 | `UIScaleManager` con factor global persistido | HECHA | `swingui/UIScaleManager.java`; clave `uiScale` en prefs |
| 2.4 | Reaplicar escala a todas las ventanas (`updateComponentTreeUI`) | HECHA | en `UIScaleManager.apply()` |
| 2.5 | Nuevos iconos + menú de zoom (subir/bajar/reset UI y canvas) | HECHA | toolbar `ppBar` + nuevo menú "View" |
| 2.6 | Commit Fase 2 | EN_PROGRESO | |

## Fase 3 — Modo oscuro + FlatLaf

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 3.1 | Añadir dependencia FlatLaf y fijarla como L&F por defecto | HECHA | `com.formdev:flatlaf:3.5.4`; `ThemeManager` en `main()` |
| 3.2 | Selector Claro/Oscuro/Sistema persistido | HECHA | submenú "Theme" en el menú View; clave `theme` en prefs |
| 3.3 | Paleta de tema compartida | HECHA | `swingui/ThemePalette.java` |
| 3.4 | Theming de `PlayPen`, `TablePaneUI`, `RelationshipUI` | HECHA | `PlayPen.applyThemePalette()`; `BasicTablePaneUI` lee la paleta; relaciones heredan de PlayPen |
| 3.5 | Commit Fase 3 | EN_PROGRESO | |

## Fase 4 — DDL

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 4a.1 | Acción `CopyDDLAction` en popup de `TablePane` y `DBTree` | HECHA | ítem "Copy DDL" en ambos popups |
| 4a.2 | Diálogo preview DDL con Copiar/Guardar | HECHA | reutiliza `SQLScriptDialog` |
| 4a.3 | Generar DDL del objeto con índices/FKs/comentarios relacionados | HECHA | `generateDDLStatements(lista)` por tabla |
| 4a.4 | Commit Fase 4a | EN_PROGRESO | |
| 4b.1 | `ddl/DDLParser.java` (tokenizer + recursive descent) | HECHA | multi-dialecto, comentarios, identificadores entre comillas, multi-statement |
| 4b.2 | Diálogo "Crear tabla desde DDL" (dialecto + preview + crear) | HECHA | `CreateTableFromDDLDialog` con Parse/Preview/Create |
| 4b.3 | Acción de menú + manejo de errores con línea/columna | HECHA | `CreateTableFromDDLAction` en menú Tools; errores con línea/columna; sin tablas parciales (rollback) |
| 4b.4 | Tests `DDLParserTest` | HECHA | 8 tests, 0 fallos |
| 4b.5 | Commit Fase 4b | EN_PROGRESO | |
| 4c.1 | Campo de notas por tabla (propiedad de `SQLObject`) | HECHA | `SQLTable.ddlNotes` (`@Accessor`/`@Mutator`, persistido) |
| 4c.2 | UI de notas en Table Properties y diálogo de copia | HECHA | campo "DDL notes" en `TableEditPanel` |
| 4c.3 | Anexar notas al DDL con validación/sanitización | HECHA | `DDLCommentUtils` + `GenericDDLGenerator.addDdlNotes` |
| 4c.4 | Tests de validación de notas | HECHA | `DDLCommentUtilsTest` (5 tests) |
| 4c.5 | Commit Fase 4c | EN_PROGRESO | |

## Fase 5 — Estilo DbSchema

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 5.1 | Reescritura `BasicTablePaneUI` (tarjeta moderna) | HECHA | sombra, esquinas redondeadas, header con borde inferior, título en negrita |
| 5.2 | Iconos PK/FK/NN y filas alternas | HECHA | filas alternas según `ThemePalette`; tags PK/FK/NN ya vienen de `columnText` |
| 5.3 | Modernizar `ColumnEditPanel` (tabs, búsqueda tipos, validación) | HECHA | búsqueda de tipos en el popup del selector (`PopupJTreeAction.withSearch`) |
| 5.4 | Commit Fase 5 | HECHA | `68020f0c` + submódulo `e3d9d6b0` |

## Fase 6 — Verificación

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 6.1 | Tests unitarios nuevos | HECHA | `DDLParserTest`, `DDLCommentUtilsTest` |
| 6.2 | `gradlew test` + documentar fallos flaky | HECHA | App 1172/0; Librería 971/2 pre-existentes documentados |
| 6.3 | Checklist manual de UI | HECHA | ver CONTEXTO §9 |
| 6.4 | Commit final | EN_PROGRESO | |

## Fase 8 — Corrección de errores detectados en uso

> Causas raíz y detalle en CONTEXTO §10. Cada tarea es independiente y commiteable por separado.

| ID | Tarea | Estado | Notas |
|----|-------|--------|-------|
| 8.1 | Corregir `PopupJTreeAction.searchTree` (ClassCastException en búsqueda de tipos) | HECHA | `sqlpower-library` `PopupJTreeAction.java` ahora recorre el `TreeModel` (no castea a `TreeNode`). Test: `PopupJTreeActionTest` (2). Ver CONTEXTO §10.1 |
| 8.2 | Arreglar estela del nombre de tabla al arrastrar | HECHA | `PlayPen.FloatingContainerPaneListener` repinta el playpen completo durante el drag. Ver CONTEXTO §10.2 |
| 8.3 | Ancho mínimo de tabla según texto más largo + mínimo estándar | HECHA | `BasicTablePaneUI.getPreferredSize` mide el título en negrita y aplica mínimo de ~30 chars. Tests: `TestTablePane` (2). Ver CONTEXTO §10.3 |
| 8.4 | Color de tabla por defecto celeste (no blanco) | HECHA | `ContainerPane` usa `ThemePalette.getDefaultTableColour()` (celeste claro/oscuro según tema). Test: `TestTablePane` (1). Ver CONTEXTO §10.4 |
| 8.5 | Texto/tooltip correcto en botones `-`/`+` de escala UI | HECHA | `ArchitectFrame` ppBar muestra texto `+`/`-`/`100%` en los botones de escala. Ver CONTEXTO §10.5 |
| 8.6 | Escalado UI global debe cubrir menús e iconos de Project Toolbar | HECHA | `UIScaleManager` re-escala iconos (`scaledIcon`) de botones/menús/labels al aplicar escala. Test: `TestUIScaleManager` (2). Ver CONTEXTO §10.6 |
| 8.7 | `CheckForUpdateAction` apuntar a GitHub Releases (evita timeout) | HECHA | usa `https://api.github.com/repos/SQLPower/power-architect/releases/latest`, parsea `tag_name` (strip `v`/`V`), abre `https://github.com/SQLPower/power-architect/releases`. Test: `TestCheckForUpdateAction` (3). Ver CONTEXTO §10.7 |
| 8.8 | Adoptar convención de versión con prefijo `V` (V2.0.11) | HECHA | `Version`/`ArchitectVersion` aceptan/conservan el prefijo `V`; `build.gradle` y `architect.version.properties` en `V2.0.11`. Tests: `VersionTest` (4, librería), `ArchitectVersionTest` (13). Ver CONTEXTO §10.8 |
