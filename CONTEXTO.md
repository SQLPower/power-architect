# CONTEXTO.md — Contexto acumulado del proyecto

> Este archivo persiste el contexto de negocio, técnico y las decisiones tomadas a lo
> largo de las sesiones, de modo que el proyecto pueda retomarse aunque se pierda el
> estado de la conversación. Actualizarlo al cierre de cada fase.

## 1. Origen y objetivo

Se toma la versión actual del repositorio **power-architect** (SQL Power Architect) y su
dependencia **sqlpower-library** (en una primera pasada se decidió incluirla en el
proyecto base como submódulo vendido, aunque aumenta el peso del repo).

Requerimientos solicitados:
1. Migrar a **JDK 21** y, en consecuencia, actualizar dependencias de proyectos aledaños,
   versiones de librerías/frameworks (Gradle, Spring, etc.).
2. **Zoom** de la aplicación con Ctrl + rueda del mouse (arriba/abajo) para agrandar o
   achicar la UI **incluyendo fuentes**.
3. **Modo oscuro**.
4. **Crear tabla desde un DDL dado**.
5. **Copiar los DDL individuales** por cada tabla u objeto sin tener que exportar todo.
6. **Comentarios en línea en el SQL de cada tabla**, validando que no rompa el SQL.
7. **Modernizar el estilo visual** de las tablas y los popups de llenado de columnas a un
   estilo parecido a **DbSchema** (https://dbschema.com/).

Requerimientos de proceso (permanentes):
- Crear y mantener `PLAN.md`, `TAREAS.md`, `CONTEXTO.md` (y `preguntas.md` si hay preguntas).
- No detenerse hasta terminar; solo pausar para preguntar.
- Commitear al cierre de cada fase.

## 2. Estado del repositorio al inicio

- Rama de trabajo: `develop`. Remoto: `origin` (github.com/SQLPower/power-architect).
- Último commit al inicio de esta sesión: `5feda720 Migrate app to Gradle + JDK 21,
  vendor sqlpower-library submodule`.
- La migración **Ant → Gradle** y a **JDK 21** ya estaba empezada y commiteada (sesión
  previa, antes de un corte de luz que borró el plan).
- `sqlpower-library` es submódulo git vendido (commit propio `a313c93b Modernize to
  Gradle + JDK 21`).
- Gradle **8.14.2** (wrapper). Toolchain Java **21** (`C:\Program Files\Java\jdk-21`).
  El `java` del PATH es 11 (solo afecta a comandos manuales; Gradle usa el toolchain 21).
- Cambios **sin commitear** en `build.gradle` que intentan arreglar el classpath de las
  clases generadas en el runtime de tests.
- Tests `ArchitectAutoTests` fallan con `SPPersistenceException / ClassNotFoundException`
  (clases generadas por el annotation processor ausentes en runtime de test).
- `AGENTS.md` está desactualizado (sigue describiendo Ant).

## 3. Arquitectura relevante (hallazgos de exploración)

- **Main frame:** `src/main/java/ca/sqlpower/architect/swingui/ArchitectFrame.java`.
  - L&F fijado al del sistema en `main` (~línea 1374):
    `UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())`.
- **Canvas / workspace:** `PlayPen.java` (JComponent scrollable), `TablePane.java` +
  `BasicTablePaneUI.java` (dibujo de tablas), `RelationshipUI`/`BasicRelationshipUI`
  (dibujo de FKs).
  - **Zoom ya existe** pero solo en el canvas: `PlayPen.zoom`, `setZoom`/`getZoom`,
    `g2.scale(zoom, zoom)`; coordenadas `zoomPoint`/`unzoomPoint`/`zoomRect`.
    Acciones: `ZoomAction`, `ZoomResetAction`, `ZoomToFitAction`.
- **DDL:** `ddl/DDLGenerator.java` (interfaz), `ddl/GenericDDLGenerator.java` (base de
  todos los dialectos: DB2, H2, HSQLDB, MySQL, Oracle, Postgres, SQLServer, Liquibase).
  - `generateDDLStatements(Collection<SQLTable>)`, `addTable(SQLTable)`,
    `addComment(SQLObject)`.
  - Export actual **solo de todo el modelo**: `swingui/action/ExportDDLAction.java`
    (+ `DDLExportPanel`, `SQLScriptDialog`). **No existe** copia por tabla/objeto.
- **Parser SQL/DDL:** **no existe ninguno** en el repo. Hay que construirlo.
- **Comentarios:** cada generador emite comentarios (`-- Comment for ...`, `COMMENT ON ...`,
  `ALTER TABLE ... COMMENT`); nunca se parsean/limpian. `ddl/critic/CommentCritic`.
- **Diálogos de edición:**
  - `TableEditPanel` (propiedades de tabla).
  - `ColumnEditPanel` (editor de columnas; lo abren `EditColumnAction`, `InsertColumnAction`).
  - `DefaultColumnPanel` (defaults de columna nueva).
  - `RelationshipEditPanel`, `IndexEditPanel`, `CheckConstraintTable`, `ColumnMappingPanel`,
    `TabbedDataEntryPanel` (agrupa varios `DataEntryPanel`), `SQLScriptDialog` (preview SQL).
- **Persistencia del modelo:** XML (`ProjectLoader`, `SwingUIProjectLoader`,
  `ArchitectSwingProject`). El annotation processor `SPAnnotationProcessor` genera helpers
  `*SPPersisterHelper` en un segundo pase javac.

## 4. Dependencias vendidas (a modernizar)

- `lib/` (runtime), `buildlib/` (compile), `sqlpower-library/lib/main/`.
- Versiones antiguas detectadas: `guava-11.0.2`, `spring-security-core-2.0.5.RELEASE`,
  `spring-core` (sin versión), `log4j-1.2.15`, `httpclient-4.0`, `commons-httpclient-3.1`,
  `commons-*` viejos, `servlet-api-3.0`, `junit-4.12`, `miglayout-3.7-swing`,
  `rsyntaxtextarea` (sin versión), `mondrian-3.14.0.0-12`, `olap4j-0.9.7.302`,
  `kettle-*3.2.2`, `iText-2.0.8`, `edtftpj-1.5.4`, `piccolo/piccolox`, `forms-1.3.0`.
- **Uso real de Spring:** solo ~10 imports de `org.springframework.security.*`
  (`AccessDeniedException`, `GrantedAuthority`, `UserDetails`) en `enterprise/`.
  No hay inyección de dependencias Spring. Spring Core no se usa directamente en app.
- **Uso de servlet:** `javax.servlet.*` en `sqlpower-library` (`DBConnection`, `Web`,
  `SQLPowerUtils`, `Servlets`, `XMLRMIDBCSSource`).

## 5. Decisiones tomadas (con el usuario)

| # | Tema | Decisión |
|---|------|----------|
| D1 | Estrategia de dependencias | Migrar a **coordenadas Gradle/Maven**. Excepciones vendidas documentadas donde no haya equivalente limpio (kettle, mondrian/olap4j, iText2, edtftpj, forms/piccolo). |
| D2 | Spring | **Migrar solo la superficie mínima** `org.springframework.security.*` a API propia; retirar `spring-core` y `spring-security-core` vendidos. No migrar a Spring Security 6. |
| D3 | Look & Feel / modo oscuro | **Añadir FlatLaf** (Apache 2.0); temas claro/oscuro modernos. |
| D4 | Zoom | **Ctrl+rueda = zoom del canvas** (conservando las acciones actuales) y **teclado numérico `+`/`-`** para zoom del canvas. Además, **factor global de UI** (fuentes, diálogos, árbol) con **nuevos iconos y menú de zoom**. Ambos factores persistidos. |
| D5 | Crear tabla desde DDL | Parser **CREATE TABLE multi-dialecto** (Postgres/MySQL/SQLServer/Oracle/Genérico): columnas, tipos con parámetros, NOT NULL, DEFAULT, PK (inline y de tabla), UNIQUE, CHECK, FK REFERENCES; ignora comentarios; identificadores entre comillas; multi-statement. |
| D6 | Copiar DDL | **Menú contextual + diálogo de preview** (Copiar/Guardar) para la tabla/objeto seleccionado, incluyendo índices/FKs/comentarios relacionados. |
| D7 | Comentarios en SQL | **Notas por tabla que se anexan al DDL generado**, validando/sanitizando que no rompan la sintaxis (`/* */`, `--`, terminadores). |
| D8 | Estilo visual | **Tablas modernas en el canvas + diálogos de columna limpios** (estilo DbSchema). |
| D9 | Build Ant | **Reemplazar por Gradle**; conservar `build.xml` marcado como deprecado. |
| D10 | Idioma de docs | **Español** (términos técnicos en inglés cuando aplique). |
| D11 | Commits | **Commitear al cierre de cada fase.** |
| D12 | `caracteristicas_futuras.md` | Mantener un backlog de features futuras; al planificarlas, moverlas al plan/contexto y eliminarlas de ese archivo. |

## 6. Especificaciones técnicas por feature

### Zoom
- Ctrl+rueda sobre el canvas → `PlayPen.setZoom` anclado a la posición del cursor.
- Teclado numérico `+`/`-` → zoom del canvas.
- Factor global de UI persistido (escala de fuentes de `UIManager` + `updateComponentTreeUI`
  sobre ventanas abiertas). Nuevos iconos y menú.
- Conservar `ZoomAction`, `ZoomResetAction`, `ZoomToFitAction`.

### Modo oscuro / estilo
- FlatLaf con temas Light/Dark/System; selector persistido.
- Paleta de tema compartida para componentes custom-painted (`PlayPen`, `TablePaneUI`,
  `RelationshipUI`), que no se benefician automáticamente de FlatLaf.
- Estilo de tabla: header de color, esquinas redondeadas, sombra, filas alternas,
  iconos PK/FK/NN, columna de tipo, mejor espaciado, hover/selección.

### Crear tabla desde DDL
- Parser propio en `ddl/DDLParser.java`; diálogo con selector de dialecto, preview y crear.
- Errores reportados con línea/columna; no crear tablas parciales.

### Copiar DDL por objeto
- Acción en popup de `TablePane` y `DBTree`; diálogo reutilizando `SQLScriptDialog`.

### Notas por tabla
- Almacenadas como propiedad de `SQLObject` (evita tocar el annotation processor).
- Editables en Table Properties y en el diálogo de copia; anexadas al DDL como comentarios
  tras validar.

## 7. Referencias

- DbSchema: https://dbschema.com/ (scrapeado: ER diagrams, documentación HTML5, sync de
  esquema, diseño visual, SQL editor, generación de datos, AI assistant, Git workflow).
- `AGENTS.md` (convenciones del repo: logger primero, 4 espacios, sin prefijo `I`).

## 8. Bitácora de avance

### Fase 0 — Persistencia y saneamiento (HECHA, commit `1f370cc1`)
- Creados `PLAN.md`, `TAREAS.md`, `CONTEXTO.md`, `caracteristicas_futuras.md`.
- Limpiados artefactos de la raíz (`app-test*.log`, `TestingJob.kjb`, `testKettle*`).
- `AGENTS.md` actualizado a Gradle/JDK 21.

### Fase 1 — JDK 21 + Gradle + dependencias (EN_PROGRESO)
Hallazgos y arreglos de la parte de build/tests:
- **Causa de los fallos `SPPersistenceException/ClassNotFoundException`:** las clases
  generadas por el annotation processor (`*SPPersisterHelper`) no estaban en el
  *runtime classpath* de los tests. Ya corregido en `build.gradle`
  (`runtimeClasspath += sourceSets.main.output`, `compileAppGenerated` output en
  `test.classpath`, y `testImplementation` de la configuración `testClasses` de la
  librería).
- **Bloqueos de archivos en Windows:** el language server de VSCode (redhat.java) y/o
  daemons residuales de Gradle mantenían handles sobre `build/`, provocando
  `AccessDeniedException` / `Unable to delete directory`. Solución operativa: correr sin
  procesos Java externos y, si aparece, borrar `build/` manualmente y reintentar.
- **Selección de tests:** Ant corría solo las 4 suites de entrada
  (`ArchitectBusinessTestSuite`, `ArchitectSwingTestSuite`, `ArchitectAutoTests`,
  `ProfileTests`). Gradle descubría *todas* las clases, incluyendo clases base JUnit3 sin
  métodos de test (`ArchitectTestCase`, `TestArchitectSwingWorker`) que producían
  `No tests found in ...`. Se configuró `test { include ... }` con esas 4 suites en
  `build.gradle`.
- **Layering `ddl` ↔ `swingui`:** `DDLGenerator`/`GenericDDLGenerator` importaban
  `ArchitectSwingSession` solo para asignarlo a un campo nunca leído. Se eliminó el
  parámetro `ArchitectSwingSession` de `generateDDLScript(...)` (y el campo/getter
  vestigial). Corrige `ArchitectCoreDependencyTest.testDDLDependencies`.
- **Tests de BD externa excluidos** del suite auto (`ArchitectAutoTests`): 
  `SQLServerDDLGeneratorTest` (requiere SQL Server vivo en `dhcp-110:1433`) y
  `KettleJobOutputToRepoTest` (Kettle 3.2.2 genera `ALTER TABLE ... MODIFY`, incompatible
  con HSQLDB, y deja estado persistente). Documentado; alineado con la intención de
  `build.gradle`.
- **`SQLRelationshipComparatorTest.testCompareColumn`:** fallaba por dependencia de orden
  de ejecución (estado estático `SQLColumn.defaultName`, que otros tests cambian). Se
  resetea `SQLColumn.setDefaultName(null)` en `setUp` y se usan aserciones por signo.
- **Librería `sqlpower-library`:** se anotó `SQLRelationship.getMappings()` como
  `@Transient @Accessor` (era un getter sin anotar; 5 de los 7 fallos pre-existentes
  bajaron). Quedan **2 fallos pre-existentes** en `TestSQLRelationship`
  (`testAllSettersAreUndoable`, `testPersisterCreatesNewObjects`), en la capa de
  persistencia/undo, ajenos a las 7 features solicitadas; documentados como pendientes.
- **Estado de tests:** App 1159 tests / 0 fallos. Librería 971 tests / 2 fallos.

### Pendientes conocidos
- 2 fallos pre-existentes de `TestSQLRelationship` (librería).
- Migrar dependencias vendidas a coordenadas Maven (en curso).

### Modernización de dependencias (Fase 1, parte 2)
- **Spring Security eliminado** (D2): se reemplazó la superficie `org.springframework.security.*`
  por API propia en la librería:
  - `ca.sqlpower.enterprise.AccessDeniedException` (RuntimeException).
  - `ca.sqlpower.enterprise.client.GrantedAuthority` (interfaz con `getAuthority()`).
  - `ca.sqlpower.enterprise.client.UserDetails` (interfaz con los 7 métodos de Spring).
  Se actualizaron los 6 usos de `AccessDeniedException` (app + librería) y
  `Group`/`User` (implements). Se eliminó `spring-security-core-2.0.5.RELEASE.jar`.
- **`spring-core.jar` se conserva vendido**: Kettle 3.2.2 lo requiere en runtime
  (`org.springframework.core.io.ResourceLoader`). Excepción documentada.
- **Migradas a coordenadas Maven** (eliminados los JARs vendidos equivalentes):
  - `log4j-1.2.15` → `ch.qos.reload4j:reload4j:1.2.25` (drop-in, misma API `org.apache.log4j`).
  - `guava-11.0.2` → `com.google.guava:guava:33.3.1-jre`.
  - `commons-codec-1.10` → 1.17.1; `commons-io-1.4` → 2.16.1; `commons-logging` → 1.3.4.
  - `httpclient-4.0`/`httpcore-4.0.1`/`httpmime-4.0` → 4.5.14/4.4.16/4.5.14.
  - `jsr305.jar` → `com.google.code.findbugs:jsr305:3.0.2`.
  - `mail-1.4.1.jar` eliminado (ya reemplazado por jakarta.mail).
- **Excepciones vendidas documentadas** (sin equivalente Maven limpio o cambio de API
  riesgoso): kettle 3.2.2, mondrian, olap4j, iText2, edtftpj, forms/piccolo, velocity,
  jfreechart/jcommon, eigenbase, jakarta-regexp, gsbase, UmlGraph, ant,
  commons-httpclient-3.1 (1 uso), commons-lang 2.4, commons-digester, miglayout,
  rsyntaxtextarea, jmdns, opencsv, servlet-api 3.0, commons-vfs, xercesImpl,
  **commons-beanutils + commons-collections** (migrarlos a 1.9.4/3.2.2 rompió tests de
  rollback por cambio en el orden de introspección de `PropertyUtils`; se revirtió).
- **Ajustes de build requeridos por la migración:**
  - Los source sets custom (`apt`, `generated`) y los tasks de anotación ahora incluyen
    `sourceSets.main.compileClasspath` (guava/log4j ya no están en los fileTrees vendidos).
  - `distTar`/`distZip` con `duplicatesStrategy = EXCLUDE` (jars duplicados entre las
    carpetas vendidas).
- **Estado final de tests tras la migración:**
  - App: 1159 tests / 0 fallos.
  - Librería: 971 tests / 2 fallos pre-existentes (`testAllSettersAreUndoable`,
    `testPersisterCreatesNewObjects`), documentados.
  - `GenericNewValueMaker` corregido para manejar un `Color` nulo (el test-util fallaba con
    NPE cuando una propiedad de color tenía valor null).
  - `TestSQLColumn` ajustado al retorno genérico de `BeanUtils.describe`.

### Fase 2 — Zoom (HECHA)
- **Ctrl+rueda** sobre el canvas: nuevo `PlayPen.ZoomOnWheelListener`. Hace zoom del canvas
  **anclado al cursor** (el punto del modelo bajo el cursor permanece fijo ajustando el
  scroll del `JScrollPane`). Usa `setZoom(zoom * 2^0.25)` por notch, con clamp `[0.05, 100]`
  solo para la rueda.
- **Teclado numérico + / −**: bindings `VK_ADD`/`VK_SUBTRACT` (sin modificador) en el root
  pane de `ArchitectFrame` → `zoomInAction`/`zoomOutAction`. Se conservan los atajos
  existentes `Ctrl+Shift+=` y `Ctrl+-` de `ZoomAction`.
- **Escala global de UI** (D4): nuevo `swingui/UIScaleManager.java` que re-deriva todas las
  fuentes de `UIManager` (desde un caché de fuentes base) y re-aplica `updateComponentTreeUI`
  + repaint a todas las ventanas. Rango `[0.6, 2.5]`; safe en headless.
- **Acciones**: nuevo `swingui/action/UIScaleAction.java` (paso geométrico 1.08). Tres
  acciones: aumentar / disminuir / resetear escala. Persistidas bajo la clave `uiScale`
  (mismo mecanismo que `zoom` en `PlayPen`).
- **UI**: las 3 acciones de escala + las 4 de zoom del canvas se añadieron al toolbar del
  playpen (`ppBar`) y a un nuevo menú **"View"** (`ArchitectFrame.viewMenu`). Se reutilizan
  los iconos `zoom_in`/`zoom_out`/`zoom_reset`.
- Nota: `setZoom()` NO aplica clamp (hay proyectos guardados con `zoom=12.3` en los tests);
  el clamp es exclusivo del gesto de rueda.

### Fase 3 — Modo oscuro + FlatLaf (HECHA)
- **FlatLaf 3.5.4** (D3) como L&F moderno (Apache 2.0). `com.formdev:flatlaf` en `build.gradle`.
- **`swingui/ThemeManager.java`**: temas `SYSTEM | LIGHT | DARK`. `LIGHT` → `FlatLightLaf`,
  `DARK` → `FlatDarkLaf`, `SYSTEM` → L&F nativo del SO. Persistido en prefs
  (`Preferences.userNodeForPackage(ArchitectFrame.class)`, clave `theme`). Aplicado en
  `ArchitectFrame.main()` antes de crear la ventana. Safe en headless.
- **Selector**: submenú **"Theme"** dentro del menú **View** (`ArchitectFrame.setTheme(...)`)
  con ítems radio. Al cambiar tema: se re-aplica el factor de escala de UI contra las fuentes
  nuevas (`UIScaleManager.invalidate() + refresh()`) y se actualiza la paleta del canvas.
- **`swingui/ThemePalette.java`**: paletas claro/oscuro con colores para canvas (fondo/foreground),
  cuerpo de tabla, borde, selección y fila alternada. `ThemePalette.current()` según el tema.
- **Componentes custom-painted**:
  - `PlayPen.applyThemePalette()`: fondo/foreground del canvas según la paleta (llamado en el
    constructor y al cambiar tema). Las tablas y relaciones heredan esos colores vía
    `PlayPenComponent.getBackgroundColor()/getForegroundColor()`.
  - `BasicTablePaneUI`: reemplazados los `Color.WHITE`/`Color.BLACK` hardcodeados por colores
    de la paleta (fondo, texto, bordes, selección).
  - `RelationshipUI`/`BasicRelationshipUI`: usan `r.getForegroundColor()` (fallback al canvas).

### Fase 4a — Copiar DDL por objeto (HECHA)
- **`swingui/action/CopyDDLAction.java`**: genera el DDL de la(s) tabla(s) seleccionada(s) con
  un `DDLGenerator` del target DB (`DDLUtils.createDDLGenerator(targetDb.getDataSource())`),
  vía `generateDDLStatements(tables)` (incluye columnas, PK, índices, FKs y comentarios
  según el dialecto). Muestra el resultado en un `SQLScriptDialog` reutilizado (trae botones
  **Copiar** y **Guardar**; el botón de ejecución queda disponible porque se le pasa el
  datasource). Expone `showDDL(List<SQLTable>)` para invocarse desde los popups.
- **Puntos de entrada**: ítem **"Copy DDL"** en el menú contextual de `TablePane`
  (click derecho en una tabla del canvas) y en el popup de `DBTree` (nodo de tabla).
- Claves nuevas: `CopyDDLAction.*` (bundle de acciones), `TablePane.copyDDLMenu` y
  `DBTree.copyDDLMenu` (bundle de swingui).

### Fase 4b — Crear tabla desde DDL (HECHA)
- **`ddl/DDLParser.java`**: parser propio (tokenizer + recursive descent) para
  `CREATE TABLE` multi-dialecto (PostgreSQL/MySQL/SQL Server/Oracle/Genérico). Soporta:
  columnas con tipos y parámetros, `NOT NULL`/`NULL`, `DEFAULT`, `PRIMARY KEY` (inline y de
  tabla), `UNIQUE`, `CHECK`, `FOREIGN KEY ... REFERENCES`, `CONSTRAINT`, `COLLATE`,
  `AUTO_INCREMENT`/`IDENTITY`; ignora comentarios `--` y `/* */`; identificadores entre
  comillas (`"x"`, `` `x` ``, `[x]`); varias sentencias separadas por `;`; nombres
  cualificados (catálogo/esquema.tabla). Errores con **línea/columna**
  (`DDLParser.DDLParseException`). AST en clases anidadas `ParsedTable/ParsedColumn/
  ParsedForeignKey/ParsedUniqueConstraint`.
- **`ddl/DDLTableBuilder.java`**: convierte el AST en `SQLTable` (columnas, PK, nullability,
  default, nombre de tipo original) mapeando los tipos comunes a `java.sql.Types`
  (`OTHER` + nombre original para desconocidos). Crea las relaciones FK (best-effort: si una
  tabla referenciada no está en el lote o el mapping falla, se omite y se loguea).
- **`swingui/CreateTableFromDDLDialog.java`**: pegar DDL, **Parse** (muestra preview) y
  **Create Tables** (crea en el target DB + playpen dentro de una transacción del workspace;
  rollback ante error). **`action/CreateTableFromDDLAction.java`** en el menú **Tools**.
- **`regress/.../ddl/DDLParserTest.java`**: 8 tests (tipos, constraints, comentarios,
  identificadores, multi-statement, error de sintaxis, build).

### Fase 4c — Notas por tabla anexadas al DDL (HECHA)
- **`SQLTable.ddlNotes`** (librería): propiedad persistida con `@Accessor(isInteresting=true)`/
  `@Mutator`; el persister generado la maneja. El loader de proyecto la guarda/lee:
  `SwingUIProjectLoader` escribe `<ddlNotes>` y `ProjectLoader` (Digester) la mapea a
  `setDdlNotes`.
- **`ddl/DDLCommentUtils.java`**: `validate(notes)` (rechaza caracteres de control) y
  `toCommentBlock(notes)` (emite cada línea como `-- `, de modo que las notas **nunca**
  pueden romper la sentencia SQL).
- **`GenericDDLGenerator.addDdlNotes(t)`**: después del CREATE TABLE y comentarios del
  dialecto, anexa las notas como sentencia `COMMENT`; si la validación falla, las omite con
  un warning. Aplica a export completo y a "Copy DDL" (mismo generador).
- **UI**: campo **"DDL notes (appended as comments)"** en `TableEditPanel` (propiedades de la
  tabla); valida al aplicar cambios.
- **`regress/.../ddl/DDLCommentUtilsTest.java`**: 5 tests (validación, formato, anexado al
  DDL generado, ausencia de notas).

### Fase 5 — Estilo visual tipo DbSchema (HECHA)
- **`BasicTablePaneUI` reescrito visualmente** (la matemática de layout de filas se
  conserva; `pointToItemIndex` sigue pasando):
  - Tarjeta con **sombra** sutil y **esquinas redondeadas** (según `isRounded()`).
  - **Header** con esquinas superiores redondeadas, base cuadrada, línea separadora bajo el
    título y título en **negrita**; colores según `tp.getBackgroundColor()/getForegroundColor()`
    (selección = oscurecido, como antes).
  - **Filas alternas** (par/ impar) usando `ThemePalette.getAlternatingRow()`.
  - Bordes y selección desde `ThemePalette`.
- **Búsqueda de tipos en `ColumnEditPanel`**: `PopupJTreeAction` (librería) ganó un
  constructor con `withSearch`; el popup muestra un campo de texto arriba del árbol y al
  escribir se hace scroll + selección del primer nodo que contiene el texto
  (busca recursivamente). `ColumnEditPanel` lo activa al elegir el tipo.
- Estado de tests: app 1172 tests / 0 fallos (con los tests nuevos de parser y notas).

## 9. Checklist manual de UI (verificación humana pendiente)

La suite automatizada cubre la lógica, pero estos puntos requieren verificación visual
manual (necesitan una pantalla; no se pueden validar headless):

- [ ] **Zoom canvas:** Ctrl+rueda hacia arriba/abajo sobre el diagrama agranda/achica
  anclado al cursor; el teclado numérico `+`/`−` también. Conserva los botones/atajos de
  zoom existentes (Ctrl+Shift+=, Ctrl+-).
- [ ] **Escala global UI:** botones/toolbar y menú View → aumentar/disminuir/resetear el
  tamaño de fuentes y controles de TODA la app; persiste entre sesiones.
- [ ] **Modo oscuro/claro:** View → Theme → System/Light/Dark. Los menús, diálogos y el
  árbol cambian; el canvas y las tablas dibujadas siguen el tema.
- [ ] **Copiar DDL por objeto:** click derecho en una tabla del canvas → "Copy DDL" y en el
  árbol DB (nodo tabla) → "Copy DDL". El diálogo muestra el DDL de esa tabla con sus
  índices/FKs/comentarios; los botones Copiar y Guardar funcionan.
- [ ] **Crear tablas desde DDL:** Tools → "Create Tables from DDL...". Pegar DDL multi-
  dialecto, Parse (preview), Create. Las tablas aparecen en el canvas y en el modelo;
  errores muestran línea/columna.
- [ ] **Notas por tabla:** abrir propiedades de una tabla → campo "DDL notes"; guardar; el
  DDL exportado/copiado incluye las notas como comentarios `--`; recargar el proyecto
  conserva las notas.
- [ ] **Estilo de tablas:** las tarjetas muestran header redondeado con título en negrita,
  filas alternas y borde; selección y dark mode coherentes.
- [ ] **Búsqueda de tipos:** en el editor de columnas, el selector de tipo permite escribir
  y hace scroll/selección del tipo que coincide.

### Pendientes conocidos
- 2 fallos pre-existentes de `TestSQLRelationship` en la librería (`testAllSettersAreUndoable`,
  `testPersisterCreatesNewObjects`); no bloquean las features.

### Eliminación de la dependencia de Ant (HECHA)
- **`build.xml` eliminado**. Todos los targets de Ant se portaron a Gradle:
  - `packaging.gradle` (aplicado desde `build.gradle`): user guide JavaHelp/HTML/PDF
    (`userGuideHelp`, `userGuideHtml`, `userGuidePdf`, `userguide`), `javadoc` (+`javadocUmlGraph`),
    `pmd`, `pmdCpd`, `findbugs`, `sourceTarGz`, `sourceOnlyTarGz`, `genericInstall`,
    `genericJdbcInstall`, `webstartDist`, `macJre`/`winJre`/`unixJre` (jlink), `osxAdapterJar`,
    `osxDist`/`osxDmg` (macOS), `allDist`, `allTests`.
  - El instalador de Windows ya estaba en `buildWindowsInstaller` (build.gradle).
- **Eliminados del código**:
  - Paquete `src/main/java/ca/sqlpower/architect/antbuild/` (`ArchitectVersionTask`,
    `AddLicenseToDMGTask`); la generación de `architect.version.properties` se mantiene committeada
    en `src/main/resources/ca/sqlpower/architect/` (sincronizada con `ArchitectVersion`).
  - `buildlib/ant.jar` y `buildlib/ant-junit.jar` (dejan de estar en el classpath).
  - Uso de `org.apache.tools.ant.filters.StringInputStream` en `TestSwingUIProject` →
    `ByteArrayInputStream` + UTF-8.
- **Ajuste de classpaths del annotation processing**: los source sets custom (`apt`, `generated`)
  y los tasks de anotación de ambos proyectos usan ahora `configurations.compileClasspath` de
  forma explícita (`= files(configurations.compileClasspath, vendoredMain, ...)`), en vez de
  depender del `sourceSets.main.compileClasspath` que resultaba poco fiable en este build
  multi-proyecto (a veces se resolvía vacío y javac generaba clases
  "Unresolved compilation problems").
- **Estabilidad en Windows**: `gradle.properties` ahora fija `org.gradle.workers.max=1`
  (las pasadas de anotación de ambos proyectos compiten si hay varios workers) y ya tenía
  `org.gradle.daemon=false`. Si un build se corrompe (clases generadas "Unresolved..."), detener
  daemons (`gradlew --stop`), borrar `build/` y `sqlpower-library/build/` y reintentar; el
  language server de VSCode mantiene handles de archivos que interfieren.
- **Estado de tests tras la migración**: app 1172/0; librería 971/2 pre-existentes.

### Incidente: instalador "2.0.0-SNAPSHOT" que instalaba 1.0.9 sin las features
- **Diagnóstico**: el `version` de Gradle era `2.0.0-SNAPSHOT`, mientras que la app reporta
  `2.0.11` (`ArchitectVersion` + `architect.version.properties` committeado). El nombre del
  instalador decía "2" pero el About de la app muestra la versión del jar instalado. La app
  que el usuario veía como **1.0.9** era un jar **viejo** (1.0.9 era la versión anterior al
  bump a 2.x en el commit `a7a435cd`), es decir: se instaló un jar obsoleto — o el usuario
  corrió un instalador generado antes de las features, o la instalación previa (1.0.9) no fue
  reemplazada al instalar encima.
- **Verificación realizada**: el jar empaquetado actual (`build/staging/architect.jar`)
  contiene `app.version=2.0.11` y todas las clases nuevas (`UIScaleManager`, `ThemeManager`,
  `ThemePalette`, `DDLParser`, `DDLCommentUtils`, `CopyDDLAction`, `CreateTableFromDDLDialog`,
  `ZoomOnWheelListener`, etc.), y su manifest `Class-Path` apunta a `lib/*.jar` + `jdbc/`.
- **Corrección**: se alineó `project.version` a `2.0.11` (commit `ece2410c`). El instalador
  ahora se genera como `dist/SQL-Power-Architect-Setup-Windows-2.0.11.exe`.
  (Nota: desde §10.8 la versión sigue la convención `V` del repo, por lo que el instalador
  actual se genera como `dist/SQL-Power-Architect-Setup-Windows-V2.0.11.exe`.)
- **Para el usuario**: desinstalar la versión 1.0.9 previa (Panel de control / uninstaller) o
  instalar en una carpeta nueva, y luego ejecutar el instalador 2.0.11. El About mostrará
  2.0.11 y las features (zoom, dark mode, DDL, etc.) estarán presentes.

### Instalador de Windows (HECHA)
- **`./gradlew buildWindowsInstaller`** porta el target legacy Ant
  `windows_exe_installer` a Gradle y produce
  `dist/SQL-Power-Architect-Setup-Windows-<version>.exe`:
  1. `stageForWindowsInstaller`: arma `build/staging/` con `architect.jar` (jar de la app,
     cuyo manifest `Class-Path` referencia `lib/*.jar` y `jdbc/`), `architect.exe` (launcher
     launch4j, `dontWrapJar`, requiere Java 21), `lib/` (runtime deps, excluye jars de
     build/test: gsbase, junit, hamcrest, UmlGraph, ant-junit), `jdbc/` (drivers), `LICENSE`
     e iconos.
  2. **launch4j** crea `architect.exe` (corre en JDK 21 con `--add-exports
     java.desktop/com.sun.java.swing.plaf.windows` y `--add-opens java.base/*`).
  3. **izpack 4.3.1** compila `build/installer-work/install.xml` (config Windows con
     listeners de registro, shortcuts, packs Core) → instalador `.jar`. El compilador usa
     `Pack200` (eliminado en Java 14+), por lo que corre en un **JDK ≤ 11**
     (`C:/Program Files/Microsoft/jdk-11.0.16.101-hotspot`; `-PizpackJavaHome=...` para
     sobreescribir).
  4. **launch4j** envuelve el instalador `.jar` en el `.exe` final y lo copia a `dist/`.
- El instalador izpack 4.3.1 generado arranca correctamente en Java 21 (probado). Requiere
  Java 21 instalado en la máquina destino (el launcher `architect.exe` exige `minVersion
  21.0.0`).
- Nota: en esta máquina el language server de VSCode suele mantener manejos transitorios
  sobre `build/staging`; la tarea tolera la limpieza fallida y sobrescribe.
- Dependencias que siguen vendidas (documentadas en §8): kettle, mondrian, olap4j, iText2,
  edtftpj, forms/piccolo, velocity, jfreechart, eigenbase, jakarta-regexp, gsbase, UmlGraph,
  ant, commons-httpclient-3.1, commons-lang 2.4, commons-digester, miglayout,
  rsyntaxtextarea, jmdns, opencsv, servlet-api 3.0, commons-vfs, xercesImpl,
  commons-beanutils + commons-collections, spring-core (para Kettle).
- `caracteristicas_futuras.md` mantiene el backlog de ideas alineadas con productos de mercado.

## 10. Errores detectados en uso (corrección en Fase 8)

Detectados por el usuario tras la modernización. Cada causa raíz se investigó en el
código actual; las tareas de corrección viven en `TAREAS.md` (Fase 8). Estado: todas
HECHAS con sus tests; verificación final: app 1180 tests / 2 fallos flaky pre-existentes
(`TestAutoLayoutAction.testNoCrossingLinesEasy`, `TestRelationship.testCopyConstructor`,
confirmados fallar también sin estos cambios); librería 973 / 2 pre-existentes
(`TestSQLRelationship`). Además, la versión sigue la convención `V` del repo (V2.0.11),
ver §10.8.

### 10.1 ClassCastException en la búsqueda de tipos (`PopupJTreeAction`)
- **Síntoma:** al escribir en el campo de búsqueda del selector de tipos en el editor de
  columnas: `ArchitectSwingProject cannot be cast to TreeNode`.
- **Causa raíz:** el nuevo modo `withSearch` de `PopupJTreeAction` (librería,
  `sqlpower-library/src/main/java/ca/sqlpower/swingui/PopupJTreeAction.java`) asume en
  `searchTree()`/`findMatch()` que la raíz del modelo es un `TreeNode`
  (`(TreeNode) tree.getModel().getRoot()`). Pero `ColumnEditPanel` usa `SQLTypeTreeModel`
  (app), cuya raíz es un `ArchitectProject`, no un `TreeNode`.
- **Corrección aplicada:** `searchTree()`/`findMatch()` recorren el árbol con la API de
  `TreeModel` (`getRoot()`, `getChildCount()`, `getChild()`); `findMatch` es ahora
  `static` y testable. Funciona con cualquier modelo de árbol.
- **Test:** `PopupJTreeActionTest` (librería): coincidencia con root no-`TreeNode`, sin
  coincidencia.

### 10.2 Estela del nombre de tabla al arrastrar
- **Síntoma:** al arrastrar una tabla cuyo nombre sobresale del ancho, se ve una "estela"
  del texto que solo desaparece al soltar la tabla.
- **Causa raíz probable:** durante el arrastre (`PlayPen.FloatingContainerPaneListener`)
  la tabla se mueve con `setChildPosition` + `revalidate`, pero el área sucia repintada no
  cubre siempre la extensión completa del título (el título se dibuja en negrita y puede
  exceder el ancho calculado), dejando restos al redibujar la posición anterior.
- **Corrección aplicada:** durante el drag se repinta el playpen completo
  (`pp.repaint()`), cubriendo posición anterior y nueva.
- **Test:** sin test automatizado (comportamiento visual); verificado con checklist manual.

### 10.3 Ancho mínimo de tabla
- **Síntoma:** el ancho de una tabla puede quedar menor que su texto más largo (nombre o
  columna); se desea un mínimo estándar de ~30 caracteres y que un nombre largo ajuste bien.
- **Causa raíz:** `BasicTablePaneUI.getPreferredSize()` parte de `minimumSize` (por defecto
  `Dimension()` = 0) y solo considera columnas + título, sin garantizar un mínimo de
  caracteres; además el título en negrita puede medirse más ancho que el cálculo con la
  fuente normal.
- **Corrección aplicada:** en `getPreferredSize`, se mide el título con la fuente negrita
  (mismo estilo con el que se dibuja) y se impone un ancho mínimo equivalente a 30
  caracteres de la fuente actual.
- **Test:** `TestTablePane.testPreferredWidthHasStandardMinimum` y
  `testPreferredWidthFitsLongTableName`.

### 10.4 Color de tabla por defecto
- **Síntoma:** las tablas nuevas aparecen blancas; se desea color celeste por defecto.
- **Causa raíz:** `ContainerPane` (base de `TablePane`) fijaba
  `backgroundColor = new Color(240,240,240)` en su constructor; no había celeste por defecto.
- **Corrección aplicada:** `ContainerPane` usa ahora `ThemePalette.getDefaultTableColour()`
  (celeste `#BFD6FF` en claro, azul `#2A4A6E` en oscuro). Nuevo acceso en `ThemePalette`.
- **Test:** `TestTablePane.testNewTableHasCelesteBackgroundByDefault`.

### 10.5 Texto/tooltip de los botones de escala UI (`-`/`+`)
- **Síntoma:** los botones de la barra derecha que agrandan/achican la UI global no
  muestran texto acorde; solo se ve el componente en el tooltip.
- **Causa raíz:** las acciones `UIScaleAction` se agregan al `ppBar` (JToolBar) solo con
  icono; el texto del botón queda oculto.
- **Corrección aplicada:** los botones del `ppBar` se configuran con `setHideActionText(false)`
  y texto corto `+` / `-` / `100%`.
- **Test:** sin test automatizado (comportamiento visual).

### 10.6 Alcance del escalado UI global
- **Síntoma:** el escalado UI solo afecta el nombre de la página, no el texto de los menús
  ni los iconos de la Project Toolbar.
- **Causa raíz probable:** `UIScaleManager.apply()` re-derivaba las fuentes de `UIManager`
  pero no re-escalaba los iconos (imágenes fijas de 16px) ni repintaba todos los contenedores.
- **Corrección aplicada:** `UIScaleManager` ahora re-escala los iconos
  (`ImageIcon.getScaledInstance` vía helper `scaledIcon`) de botones, menús, ítems de menú y
  labels de todas las ventanas al aplicar el factor.
- **Test:** `TestUIScaleManager` (escalado 2x y factor 1).

### 10.9 Regresión: zoom de UI rompe los iconos tras varios clics (corregido)
- **Síntoma:** tras el fix 10.6, al usar el zoom global (`+`/`-`/`100%`) en la barra derecha y
  el menú View, los iconos crecían descontroladamente y el reset a 100% no los restauraba.
- **Causa raíz:** `rescaleIcons` re-escalaba el icono **actual** (ya escalado) en cada `apply()`.
  El escalado se acumulaba (16→24→36→...) y al resetear (scale=1.0) `scaledIcon(iconoYaEscalado,
  1.0)` devolvía el tamaño actual, no el original.
- **Corrección aplicada:** `UIScaleManager` guarda el icono **original** de cada componente en un
  `WeakHashMap` (`baseIcons`) y siempre deriva el escalado desde ese original (`scaledIcon(original,
  scale)`). Así el zoom es idempotente: `+`, `+` y reset vuelven exactamente al tamaño original.
  Helper testable `rescaleButtonIcon(AbstractButton, double)`.
- **Test:** `TestUIScaleManager.testRescaleButtonIconIsIdempotentFromOriginal` (16 → 24 → 36 →
  reset → 16).

### 10.7 `CheckForUpdateAction` timeout
- **Síntoma:** al pulsar "Check for updates" → `java.net.ConnectException: Connection timed
  out` en `CheckForUpdateAction.checkForUpdate` (línea 77).
- **Causa raíz:** la acción usaba URLs HTTP antiguas y muertas
  (`http://power-architect.sqlpower.ca/current_version` y
  `http://download.sqlpower.ca/architect/current.html`). El proyecto vive ahora en
  `https://github.com/SQLPower/power-architect` y sus releases en
  `https://github.com/SQLPower/power-architect/releases`.
- **Corrección aplicada:** consulta `https://api.github.com/repos/SQLPower/power-architect/releases/latest`
  (timeout 15s, `User-Agent` propio), parsea `tag_name` (quito el prefijo `v` si existe),
  compara con `ArchitectVersion.APP_FULL_VERSION` y abre la página de releases si hay
  actualización. Errores de red muestran el mensaje `failedToCheckForUpdate` existente.
- **Test:** `TestCheckForUpdateAction` (extracción de `tag_name`, con `v`/`V` y ausente).

### 10.8 Convención de versión con prefijo `V`
- **Motivo:** el repo base usa tags de release con prefijo `V` (p. ej. `V1.9.0`); se alineó la
  versión nueva a esa convención: **V2.0.11**.
- **Cambios:**
  - `Version` (librería): acepta y conserva el prefijo `V`/`v` en `toString()`, lo ignora para
    comparación numérica; el copy-constructor copia el prefijo.
  - `ArchitectVersion`: `APP_VERSION_MAJOR = "V2"` → `APP_FULL_VERSION` = `V2.0.11`; delega en
    `Version` para el parseo.
  - `build.gradle`: `version = 'V2.0.11'` (nombre del instalador con `V`).
  - `architect.version.properties`: `app.version=V2.0.11` (About).
- **Compatibilidad:** archivos de proyecto guardados como `2.0.11` (sin `V`) se siguen leyendo;
  la comparación (`2.0.11 > 1.9.0`) no cambia.
- **Tests:** `VersionTest` (4, librería) y `ArchitectVersionTest` (13, app, con `testAppFullVersionUsesVendorVPrefixConvention`).

## 11. Localización multi-idioma (es, en, pt, zh, ja, de) — Fase 9

Implementada según el diseño `docs/superpowers/specs/2026-09-21-localizacion-multi-idioma-design.md`.
Tareas en `TAREAS.md` (Fase 9).

### 11.1 Bundles de traducción (ResourceBundle estándar de Java)
- El proyecto ya usaba `messages*.properties` + `ResourceBundle` (estándar Java). Existían
  `_de`, `_ko`, `_pt`, `_ru`.
- **Nuevos** `messages_es.properties`, `messages_zh.properties`, `messages_ja.properties` en los
  6 paquetes con bundle base (app: `architect`, `swingui`, `swingui/action`; librería: `swingui`,
  `swingui/query`, `sqlobject`). Traducciones completas (~840 claves × 3 idiomas).
- **Completados** `_de` y `_pt` con las claves faltantes. Se corrigió la clave corrupta
  `SPSUtils.SPSUtils.velocityFileType` (doble prefijo) en `messages_de.properties` de la librería.
- Codificación UTF-8 con caracteres directos (zh/ja sin escapes `\uXXXX`), coherente con JDK 21.
- Verificado por `MessagesLocalizationTest`: todos los bundles app+lib cargan en los 6 idiomas y
  contienen las claves del base.

### 11.2 Selector de idioma en Preferences
- `LocaleChooser.availableLocales` ahora lista: en, de, ko, ru, pt, **es, zh, ja**.
- `PreferencesPanel` ya persistía `DEFAULT_LOCALE`; el cambio aplica al reiniciar (comportamiento
  aceptado). `ArchitectFrame` aplica `Locale.setDefault` desde prefs al arrancar.

### 11.3 Instalador izpack multi-idioma
- `build.gradle` → `install.xml` ahora declara: `<langpack iso3="eng|spa|por|chn|jpn|deu"/>`.
- izpack 4.3.1 muestra automáticamente el selector de idioma al inicio cuando hay más de un
  langpack; paneles y botones del instalador se muestran en el idioma elegido.
- Verificado que `standalone-compiler.jar` incluye los 6 langpacks y que el jar del instalador los
  empaqueta.

### 11.4 Estado de tests / instalador
- App: 1180 tests / 2 fallos flaky pre-existentes (`TestAutoLayoutAction`, `TestRelationship`).
- Librería: 973 / 2 pre-existentes (`TestSQLRelationship`).
- Instalador regenerado: `dist/SQL-Power-Architect-Setup-Windows-V2.0.11.exe` con langpacks y
  bundles nuevos empaquetados.

## 12. Instalador Windows: directorio de instalación y desinstalación (Fase 10)

### 12.1 Diagnóstico del error reportado
- **Síntoma:** en Windows 11 el instalador instala en `C:\Program Files` en vez de
  `C:\Program Files (x86)` como antes; además el usuario no lo encuentra al desinstalar.
- **Hallazgos verificados en el código/build:**
  - El `architect.exe` (launch4j wrapper de la app) y el `.exe` del instalador son **x86 (32-bit)**
    (PE Machine `0x014C`). launch4j usa `headerType gui` sin bitness explícito.
  - El proyecto ya corre en **JDK 21** (64-bit). `architect.exe` exige `minVersion 21.0.0`.
  - El instalador izpack (`buildWindowsInstaller`) empaqueta `architect.exe` + `architect.jar` +
    `lib/*.jar` + `jdbc/`. El `install.xml` NO fija un directorio de instalación explícito
    (usa el default de izpack, derivado de la JVM que ejecuta el instalador).
- **Causa raíz (directorio):** la elección entre `Program Files` y `Program Files (x86)` depende
  de la redirección **WOW64**: un proceso 32-bit que escribe en `%ProgramFiles%` es redirigido a
  `Program Files (x86)`. El wrapper `.exe` es 32-bit, pero al lanzar la JVM de 64 bits (JDK 21)
  izpack 4.3.1 deriva el directorio por defecto desde la JVM (variable `PROGRAMFILES`), que en un
  proceso 64-bit apunta a `C:\Program Files`. Por eso ya no cae en `(x86)`.
- **Causa raíz (desinstalación):** con la mezcla actual (instalador 32-bit + JVM 64-bit), la
  entrada de desinstalación que registra izpack puede quedar en la vista `WOW6432Node` del
  registro y no ser visible/listada por el sistema 64-bit, o quedar apuntando a una ruta que el
  usuario no asocia con la instalación. Hay que validar la clave `HKLM\Software\...
  \CurrentVersion\Uninstall\...` generada.

### 12.2 Opciones de corrección
- **Opción A — Migrar a 64 bits (recomendada a largo plazo):**
  - Generar `architect.exe` y el wrapper del instalador como **x86_64** (launch4j con bitness 64
    y JRE path de 64 bits). La app ya es JDK 21 (64-bit); no hay dependencias de 32 bits
    documentadas en el código (mondrian/olap4j/kettle son jars, no DLLs nativas).
  - El directorio por defecto pasaría a ser `C:\Program Files` de forma consistente (nativo 64-bit),
    y la desinstalación se registra en la vista nativa del registro.
  - Afecta a `build.gradle` (config launch4j), validación del JRE de 64 bits, y prueba manual.
  - Estimación: media (bajo/medio riesgo); ver plan en TAREAS.md Fase 10.
- **Opción B — Mantener x86 y forzar el directorio correcto:**
  - Fijar explícitamente `C:\Program Files (x86)\SQL Power Architect` como directorio de
    instalación del instalador izpack (evitar depender de WOW64 / de la JVM que lo lanza).
  - Mantener `architect.exe` en 32-bit (compatible con JVM 32/64 de arranque).
  - La desinstalación debe validarse en la clave `WOW6432Node` correspondiente.
  - Estimación: pequeña (bajo riesgo), solo config del instalador + prueba manual.

### 12.3 Resolución adoptada (Fase 10)
- **Decisión del usuario:** Opción **A-pragmática** — mantener los wrappers de 32 bits pero
  fijar la instalación y desinstalación de forma consistente en `C:\Program Files`.
- **Hallazgo clave:** el launch4j 3.12 vendido **solo genera ejecutables de 32 bits** (cabeceras
  `head/guihead.o`, `head/head.o`, `consolehead.o` COFF `0x014C`; `ld.exe`/`windres.exe` x86).
  El paquete `launch4j-3.50-win32.zip` también es solo 32-bit (verificado empíricamente con
  `<runtimeBits>64</runtimeBits>` → el `.exe` sale x86). Generar un `.exe` x86_64 real requeriría
  compilar las cabeceras desde `head_src/` con MinGW-w64 (no disponible), por lo que la migración
  completa a 64-bit queda pendiente (ver `caracteristicas_futuras.md`).
- **Cambios aplicados en `build.gradle`:**
  1. `install.xml` → `<variable name="TargetPanel.dir.windows" value="${ENV[PROGRAMFILES]}\SQL Power Architect"/>`
     para fijar el directorio por defecto. Como la JVM que ejecuta el instalador es 64-bit (JDK 21),
     `PROGRAMFILES` resuelve a `C:\Program Files` (consistente con la app).
  2. Nuevo `installer/uac-installer.exe.manifest` con `requestedExecutionLevel=requireAdministrator`,
     referenciado vía `<manifest>` en el wrapper launch4j del instalador. Así el instalador corre
     con elevación, puede escribir en `C:\Program Files` y registrar la desinstalación correctamente.
  3. Se mantiene `<runtimeBits>64</runtimeBits>` en ambos wrappers: fuerza buscar JRE 64-bit,
     coherente con la app (JDK 21) y con la resolución de `PROGRAMFILES`.
- **Verificación:** el `.exe` del instalador contiene el manifest `requireAdministrator`; el
  `install.xml` generado incluye la variable y los 6 langpacks de idioma. Pendiente: prueba manual
  en Windows 11 (instalar en `Program Files`, localizar en Panel de control, desinstalar).

### 12.4 Nota
- La entrada "Distribución de 64 bits (x86_64)" ya existía en `caracteristicas_futuras.md`
  (Prioridad MEDIA, Estado IDEA). Este incidente la reactiva como candidata real; la migración
  completa a 64-bit sigue pendiente de un launch4j con cabeceras x64 (o toolchain MinGW-w64).

## 13. Migración de empaquetado/instaladores a jpackage (Fase 11)

### 13.1 Motivación
- El build de instaladores usa **launch4j 3.12 (2018)** e **izpack 4.3.1 (2016)**, ambos antiguos:
  - launch4j solo genera wrappers **x86 (32-bit)** (verificado: cabeceras COFF 0x14C, ld/windres x86).
  - izpack usa `java.util.jar.Pack200` (eliminado en JDK 14+) → requiere un **JDK ≤11** externo.
  - launch4j exige `--add-exports/--add-opens` y el wrapper de 32 bits mezcla el registro de
    desinstalación en `WOW6432Node` → **desinstalación inconsistente** (problema reportado).
- **jpackage** es nativo desde JDK 14+ y está disponible en el JDK 21 del proyecto (v21.0.7):
  genera `app-image`, `exe`/`msi` (Windows), `dmg`/`pkg` (macOS), `deb`/`rpm` (Linux), con
  **JRE embebido** (jlink) y desinstalación nativa correcta.

### 13.2 Hallazgos del análisis
- **App no-modular** (sin `module-info.java` en app ni librería): jpackage la empaqueta con
  `--input` + `--main-jar` + `--main-class` (genera jlink automáticamente). Main:
  `ca.sqlpower.architect.swingui.ArchitectFrame`.
- **Classpath a empaquetar:** `architect.jar` (manifest `Class-Path` legacy), `lib/` (19 jars,
  ~12.5 MB), `sqlpower-library/lib/main/` (20 jars) y `jdbc_drivers/` (7 jars). jpackage usa
  `--input` (todo el contenido se copia) y `--java-options` para classpath si hace falta.
- **Módulos jlink a validar:** `java.desktop` (Swing), `java.sql`/`java.xml` (JDBC/DDL),
  `java.management`, etc. Riesgo bajo; se valida ejecutando el app-image.
- **Windows `--type msi`** requiere **WiX Toolset** (no instalado en esta máquina). `--type exe`
  no lo requiere. `--type app-image` es el paso más simple para validar.
- **Idioma del instalador:** los langpacks de izpack (Fase 9.6) no aplican a jpackage (los
  instaladores nativos usan el idioma del SO). El selector de idioma de la app sigue funcionando.
- **Iconos:** `architect.ico` (Windows) y `Power Architect.icns` (macOS) ya existen en
  `src/main/resources/icons/`.

### 13.3 Estimación de esfuerzo (orden recomendado)
| Tarea | Esfuerzo |
|-------|----------|
| 11a.1 Task `jpackageAppImage` (Windows app-image 64-bit) | 0.5–1 día |
| 11a.2 Validar runtime jlink (Swing/JDBC/i18n/vendidos) | 0.5 día |
| 11b.1 Task `jpackageExe`/`jpackageMsi` (instalador 64-bit; WiX para msi) | 1–1.5 días |
| 11b.2 Verificar instalar + desinstalar en Windows 11 | 0.5 día |
| 11c.1 macOS `dmg`/`pkg` (CI en macOS) | 1–2 días |
| 11c.2 Linux `deb`/`rpm` (CI en Linux) | 1–2 días |
| 11d.1 Retirar launch4j/izpack del build | 0.5 día |
| 11d.2 Actualizar docs (AGENTS/README/CHANGELOG) | 0.5 día |
| 11d.3 Pruebas de regresión | 0.5–1 día |
| **Total Windows** | **2.5–4 días** |
| **Total multi-plataforma** | **5–9 días** |

### 13.4 Plan por fases
- **Fase 11a — Windows app-image con jpackage:** task Gradle `jpackageAppImage` (64-bit,
  `--type app-image`), validación de jlink y arranque de la app. **HECHA** (11a.1-11a.2).
- **Fase 11b — Instalador Windows nativo:** `--type exe`; verificación de instalación y
  **desinstalación**. **En curso:**
  - jpackage en Windows requiere **WiX Toolset** para `exe`/`msi` (el error "Invalid or
    unsupported type" en realidad es "Can not find WiX tools").
  - **WiX 3.11.2 binarios vendidos** en `ext-tools-home/wix311/` (candle.exe, light.exe) y
    añadido al PATH del exec del task `jpackageExe`.
  - `./gradlew jpackageExe` genera `dist/SQL Power Architect-2.0.11.exe` **x86-64 (64-bit)**
    (jpackage nombra el output como `<name>-<app-version>.exe`).
  - Pendiente: verificación manual de instalación en `C:\Program Files` y desinstalación
    (Configuración→Apps) en Windows 11.
- **Fase 11c — Multi-plataforma (opcional):** macOS/Linux en CI.
- **Fase 11d — Limpieza:** eliminar launch4j/izpack del build y sus configs; actualizar docs.

### 13.5 Riesgos
- jlink puede omitir módulos usados por reflection (p.ej. algunos jars vendidos); mitigar con
  `--add-modules` si el app-image falla al arrancar.
- `--type msi` requiere WiX Toolset (descarga externa); `exe` es la ruta sin dependencias nuevas.
- El app-image empaqueta el JRE completo recortado (~60-100 MB); puede ser mayor que el
  instalador actual (32 MB) pero a cambio es autónomo (no requiere JRE externo).

