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

