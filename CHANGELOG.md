# Power*Architect Changelog

All notable changes to this project since version 1.0.9 are documented in this
file. See `doc/ReleaseNotes.txt` for the release history prior to 1.0.9.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.11] - Current

Version 2.0.11 bundles the 2021 maintenance work plus the 2026 modernization
effort (Gradle + JDK 21). The version number itself was introduced in commit
`a7a435cd` (2021-01-12) together with the switch to Java OpenJDK 11.2.0; there
were no intermediate public releases between 1.0.9 and 2.0.11.

### Modernization (2026)

#### Build system and dependencies

- Migrated the build from Apache Ant to **Gradle** (wrapper included). All
  historical Ant targets were ported to Gradle and `build.xml` was removed.
- Upgraded the project to **JDK 21** (toolchain) and Gradle 8.14.2.
- Vendored `sqlpower-library` as a git submodule and integrated it as a Gradle
  subproject.
- Modernized vendored dependencies to Maven coordinates where a clean
  equivalent exists (reload4j, Guava 33, Commons Codec/IO/Logging, HttpClient
  4.5, JSR 305, Jakarta Mail). Remaining vendored jars without a clean Maven
  equivalent are documented exceptions (Kettle, Mondrian, olap4j, iText 2,
  edtftpj, forms/piccolo, etc.).
- Removed the `org.springframework.security.*` dependency surface and replaced
  it with a small project-local API (`GrantedAuthority`, `UserDetails`,
  `AccessDeniedException`). `spring-core` remains vendored only because Kettle
  3.2.2 requires it at runtime.
- Added the Windows installer task (`buildWindowsInstaller`), producing
  `dist/SQL-Power-Architect-Setup-Windows-<version>.exe` via izpack + launch4j.
- Ported the packaging/analysis targets (userguide, javadoc, pmd, pmdCpd,
  findbugs, source tar.gz, generic install, all dist) to `packaging.gradle`.
- Aligned the Gradle `project.version` with `ArchitectVersion` (2.0.11) so the
  installer name and the app's reported version match.

#### UI

- **Application zoom**: `Ctrl + mouse wheel` zooms the canvas anchored to the
  cursor; the numeric keypad `+` / `-` also zooms the canvas.
- **Global UI scale**: a persistent UI factor (fonts, dialogs, tree) with new
  toolbar buttons and a "View" menu (increase / decrease / reset).
- **Dark mode**: added FlatLaf as the look and feel with Light / Dark / System
  themes, selectable and persisted from the "View" menu.
- **Shared theme palette** (`ThemePalette`) so the custom-painted components
  (`PlayPen`, `TablePaneUI`, `RelationshipUI`) follow the active theme.
- **DbSchema-style table cards**: rounded header with bold title, subtle
  shadow, alternating rows, and theme-aware colors.
- **Type search** in the column editor: the type selector now supports typing
  to filter/scroll the type tree.

#### DDL

- **Copy DDL per object**: "Copy DDL" action on the table context menu and the
  DB tree, showing a preview dialog with Copy / Save options (includes indexes,
  FKs, and related comments).
- **Create tables from DDL**: a multi-dialect `CREATE TABLE` parser
  (PostgreSQL, MySQL, SQL Server, Oracle, Generic) with a "Create Tables from
  DDL..." dialog; errors are reported with line/column and no partial tables
  are created.
- **Per-table DDL notes**: notes are stored on the table, editable from Table
  Properties, and appended to generated DDL as validated `--` comments so they
  can never break the SQL.

#### Documentation and process

- Added project tracking docs: `PLAN.md`, `TAREAS.md`, `CONTEXTO.md`,
  `caracteristicas_futuras.md`, and `AGENTS.md` updated for Gradle + JDK 21.
- Added Spanish (Latin American) versions of the project docs
  (`WISHLIST.es`, `DEVELOPERS.es`).
- Test status: app suite green (1172 tests / 0 failures); the vendored library
  has 2 pre-existing, documented failures unrelated to these changes.

### 2021 maintenance

- Updated to run on Java OpenJDK 11.2.0 (`a7a435cd`).
- Updated Launch4j to version 3.12.
- Resolved a javadoc generation error and fixed the findbugs target classpath
  for PMD.
- Various code cleanups and logging removals.

### 2020 Kettle/PDI improvements

- Allow splitting a large Kettle/PDI job into multiple jobs by selecting the
  desired tables for each job (also fixes Java heap space issues).
- Added a sort step before a `MergeJoin` step in generated transformations.
- Added a sort key option for `SortRow` steps.
- Allow selecting the transformation output type (e.g. "Insert/Update").
- Export join keys for `MergeJoin` steps and fixed the missing `[JOIN_KEY]`
  field in the reference stream.
- Updated PMD for Java 8 and Clover to `clover-ant-4.2.0`.

### Changed

- Versioning jumped directly from `1.0.9` to `2.0.11` (no 1.1.x or 2.0.0-2.0.10
  releases were published).

## [1.0.9] - 2020-12-22

Released on GitHub. Reproduced from `doc/ReleaseNotes.txt`:

- Fixed issue of including the target (output) table as an input table in a
  generated transformation job.
- Fixed issue related to exporting `MergeJoin` keys in a transformation.
- Resolved the issue of not populating the database connection name in a Kettle
  job.
- Fixed the issue of not including a `[JOIN_KEY]` field in a reference stream.
- Improved the UI for column mapping in a Column properties dialog box.
- Enhanced saving the transformation output to different types (e.g.
  "Insert/Update") in a Kettle job.
- Split a big Kettle job into multiple jobs by allowing the user to select the
  desired tables for each job.
- Added a sort step before a `MergeJoin` step in a generated transformation.
- Updated to support Java 8.

## Older releases

For releases prior to 1.0.9, see `doc/ReleaseNotes.txt`.