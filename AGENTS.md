# AGENTS.md

Power*Architect — Java 11 / Swing desktop app (SQL data modeling + OLAP + profiling). Built with **Apache Ant ≥ 1.7** (`build.xml`). No Maven/Gradle, no CI workflows.

## Build prerequisites (non-obvious)

- **`sqlpower-library` is required and is a separate repo** (github.com/SQLPower/sqlpower-library). Copy `build.properties.example` → `build.properties` and set `sqlpower.library.home` to its path. The build copies its jars into `buildlib/`. It is **not** vendored here — `ant compile` fails without it.
- **The build path must NOT contain spaces** (XSLT help generation corrupts paths with `%20`). This repo itself lives under a path with a space (`D:\DRIVE\REPOSITORIO\...`) — use a temp checkout/worktree on a spaceless path for any build that runs `help`/`dist`.
- Targets Java 11 (PMD source language and izpack `javaversion` are 11). Runtime min is Java 8.
- `build.properties` is gitignored; `eclipse-bin/`, `build/`, `generated/`, `dist/`, `staging/` are all gitignored build outputs.

## Commands

All build via Ant: `ant <target>`.

- `ant run` — compile + launch the Swing app (main class `ca.sqlpower.architect.swingui.ArchitectFrame`)
- `ant compile` — build app classes into `build/`
- `ant junit` — compile + run test suites (writes XML+HTML reports under `dist/.../reports/junit`)
- `ant alltests` — `junit` + `pmd` + `findbugs`
- `ant compile-examples` — validate code under `example_code/`
- `ant clean` / `ant dist-clean` — remove build outputs
- `ant dist` — full distribution (all platforms, javadoc, source tarballs) — heavy, not for quick iteration

## Tests

- Tests live in `regress/` (JUnit 4, ~100 files), compiled to `build_tests`.
- Entry suites run by `junit`: `ca.sqlpower.ArchitectBusinessTestSuite`, `ca.sqlpower.ArchitectSwingTestSuite`, `ca.sqlpower.ArchitectAutoTests`, `ca.sqlpower.architect.profile.ProfileTests`.
- The Swing UI suite **fails randomly** (known issue, commented in `build.xml`) — don't chase flaky failures there.
- Build runs tests headless (`ca.sqlpower.headless=true`) and points `ca.sqlpower.architect.test.dir` at `build_tests`. DB-touching tests need JDBC drivers from `jdbc_drivers/`; HSQLDB test DB comes from `testbed/hsqldb.jar`.

## Generated code (do not hand-edit)

- `compile` runs an annotation processor (`ca.sqlpower.object.annotation.SPAnnotationProcessor`) in a second javac pass and writes sources to `generated/` (gitignored), then compiles them into the build. After editing annotated model classes, always run `ant compile`.
- Mondrian OLAP classes are **XSLT-generated from `src/main/java/ca/sqlpower/architect/olap/Mondrian.xml`** and **checked in**. Regenerate with `ant mondrianModel mondrianParser mondrianFormatter` (and `mondrianNewValueMaker mondrianPersistenceTests` for the test copies under `regress/`). Don't edit these files directly; edit `Mondrian.xml` or the XSLT, then regenerate.
- App version comes from constants in `ArchitectVersion` (read by the Ant `ArchitectVersionTask`), not from a properties file.

## Layout

- `src/main/java/ca/sqlpower/architect/` — app code. `swingui/` (UI incl. `ArchitectFrame`, `dbtree/`), `ddl/` (+ `ddl/critic/`), `olap/`, `etl/`, `profile/`, `diff/`, `transformation/`, `enterprise/`, `undo/`, `antbuild/`.
- `src/main/resources/` — icons, sounds, XSLT stylesheets, `log4j.properties.example`.
- `lib/` (runtime), `buildlib/` (compile), `jdbc_drivers/` (tests), `ext-tools-home/` (vendored tools: findbugs, pmd, clover, izpack, launch4j), `testbed/` (HSQLDB for tests).
- `doc/` — user guide as DocBook XML (`PowerArchitectUserGuide.xml`) + XSLT toolchain; `html/overview.html` for javadoc.

## Conventions

- Sun-style conventions with documented exceptions in `DEVELOPERS` (top-level project file). Key ones: log4j `Logger` field first; 4-space indentation using spaces; no `Interface`/`I` prefix on interfaces (use `Foo` / `FooImpl`); catch/event vars named `e`/`ex`; single-line guard `if (cond) return;` is preferred, but multi-line conditions require braces.
- Working branch is `develop`; remote is `origin` (github.com/SQLPower/power-architect).