# AGENTS.md

Power*Architect — Java 21 / Swing desktop app (SQL data modeling + OLAP + profiling). Built with **Gradle** (wrapper included). There is no Ant build: `build.xml` was removed and every historical Ant target has a Gradle equivalent.

## Build prerequisites (non-obvious)

- **`sqlpower-library` is a vendored git submodule** at `sqlpower-library/` (from github.com/SQLPower/sqlpower-library). It is included as a Gradle subproject by `settings.gradle`; you do **not** need a separate checkout or `build.properties`. Run `git submodule update --init` after cloning.
- **JDK 21 toolchain.** Both projects declare `java.toolchain.languageVersion = 21`. Gradle will locate/install a JDK 21 toolchain (a JDK 21 is present at `C:\Program Files\Java\jdk-21` on this machine). The `java` on `PATH` may be older — that's fine, Gradle uses the toolchain.
- Some vendored jars under `lib/`, `buildlib/` and `sqlpower-library/lib/main/` are still on the classpath. Modernizing them to Maven coordinates is tracked in `PLAN.md`/`TAREAS.md`; a few have no clean Maven equivalent and remain vendored on purpose.
- XSLT-based help generation historically required a path **without spaces** (paths get corrupted with `%20`). Avoid spaces in the checkout path for distribution builds.
- On Windows, stale Gradle daemons and the IDE language server can intermittently hold file locks that corrupt compilations (see `CONTEXTO.md`). `gradle.properties` pins single-worker mode; if a build misbehaves, run `gradlew --stop`, delete `build/` and `sqlpower-library/build/`, and retry.

## Commands

All via the Gradle wrapper: `./gradlew <task>` (Windows: `gradlew.bat <task>`).

- `./gradlew run` — compile + launch the Swing app (main class `ca.sqlpower.architect.swingui.ArchitectFrame`)
- `./gradlew build` — compile + assemble (includes the annotation-processing passes)
- `./gradlew test` — compile + run the test suites
- `./gradlew compileExamples` — validate code under `example_code/`
- `./gradlew clean` — remove build outputs
- `./gradlew mondrianModel mondrianParser mondrianFormatter` — regenerate Mondrian OLAP sources from `Mondrian.xml`
- `./gradlew buildWindowsInstaller` — build the Windows installer (izpack + launch4j). Produces `dist/SQL-Power-Architect-Setup-Windows-<version>.exe`. Notes:
  - The izpack compiler (4.3.1) uses `java.util.jar.Pack200` (removed in Java 14+), so it must run on a JDK ≤ 11; the Gradle daemon runs on JDK 21, so the task points at a JDK 11 (override with `-PizpackJavaHome=<JDK 11 home>`).
  - launch4j runs on JDK 21 with a few `--add-exports`/`--add-opens` flags (handled by the task).
- Packaging/analysis tasks ported from the removed Ant build (see `packaging.gradle`): `userguide` (JavaHelp + PDF), `userGuideHtml`, `javadoc`, `pmd`, `findbugs`, `sourceTarGz`, `genericInstall`, `allDist`.

## Tests

- Tests live in `regress/` (JUnit 4 + JUnit 5 via the vintage engine), compiled into the app's `test` source set. The library's own tests live in `sqlpower-library/src/test/java`.
- Entry suites: `ca.sqlpower.ArchitectBusinessTestSuite`, `ca.sqlpower.ArchitectSwingTestSuite`, `ca.sqlpower.ArchitectAutoTests`, `ca.sqlpower.architect.profile.ProfileTests`.
- The Swing UI suite **fails randomly** (known issue) — don't chase flaky failures there.
- Tests run headless (`ca.sqlpower.headless=true`). DB-touching tests need JDBC drivers from `jdbc_drivers/`; the HSQLDB test DB comes from `testbed/hsqldb.jar`.
- The app's generated persister classes must be on the test runtime classpath (see `compileAppGenerated` in `build.gradle`).

## Generated code (do not hand-edit)

- The build runs an annotation processor (`ca.sqlpower.object.annotation.SPAnnotationProcessor`) in a second javac pass (`runAppAnnotationProcessor` + `compileAppGenerated`), writing sources to `build/generated-src` and compiling them into the build. After editing annotated model classes, re-run `./gradlew build`.
- Mondrian OLAP classes are **XSLT-generated from `src/main/java/ca/sqlpower/architect/olap/Mondrian.xml`** and **checked in**. Regenerate with `./gradlew mondrianModel mondrianParser mondrianFormatter` (and the `mondrianNewValueMaker`/`mondrianPersistenceTests` tasks for the test copies under `regress/`). Don't edit these files directly; edit `Mondrian.xml` or the XSLT, then regenerate.
- App version comes from constants in `ArchitectVersion`, not from a properties file.

## Layout

- `src/main/java/ca/sqlpower/architect/` — app code. `swingui/` (UI incl. `ArchitectFrame`, `dbtree/`), `ddl/` (+ `ddl/critic/`), `olap/`, `etl/`, `profile/`, `diff/`, `transformation/`, `enterprise/`, `undo/`.
- `src/main/resources/` — icons, sounds, XSLT stylesheets, `log4j.properties.example`.
- `sqlpower-library/` — vendored submodule (its own Gradle build, `lib/main/` vendored jars, `src/apt/java` annotation processor).
- `lib/` (runtime), `buildlib/` (compile), `jdbc_drivers/` (tests), `ext-tools-home/` (vendored tools: findbugs, pmd, clover, izpack, launch4j), `testbed/` (HSQLDB for tests).
- `doc/` — user guide as DocBook XML (`PowerArchitectUserGuide.xml`) + XSLT toolchain; `html/overview.html` for javadoc.

## Project tracking files

- `PLAN.md` — high-level execution plan and per-phase status.
- `TAREAS.md` — atomic tasks and their states.
- `CONTEXTO.md` — accumulated context, decisions, and technical specs.
- `caracteristicas_futuras.md` — backlog of not-yet-planned features.
- `preguntas.md` — open questions for the user (created only when needed).

## Conventions

- Sun-style conventions with documented exceptions in `DEVELOPERS` (top-level project file). Key ones: log4j `Logger` field first; 4-space indentation using spaces; no `Interface`/`I` prefix on interfaces (use `Foo` / `FooImpl`); catch/event vars named `e`/`ex`; single-line guard `if (cond) return;` is preferred, but multi-line conditions require braces.
- Working branch is `develop`; remote is `origin` (github.com/SQLPower/power-architect).
