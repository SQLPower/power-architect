# Contributing to Power*Architect

Thanks for your interest in contributing! This guide explains how to work on
the codebase and how to get your changes merged via a GitHub pull request.

> **Note:** the historical `DEVELOPERS`/`WISHLIST`/`doc/ReleaseChecklist.txt`
> files describe the pre-GitHub workflow (Subversion, Google Code, mailing-list
> patches). The flow below is the current one: **Git + GitHub pull requests**.

## Repository layout

- `src/main/java/ca/sqlpower/architect/` — application code.
- `src/main/resources/` — icons, sounds, XSLT stylesheets, message bundles.
- `sqlpower-library/` — vendored git submodule (its own Gradle build).
- `regress/` — application test sources (JUnit 4/5 via the vintage engine).
- `lib/`, `buildlib/`, `jdbc_drivers/`, `ext-tools-home/`, `testbed/` — vendored
  runtime/compile/test dependencies and tools.
- `doc/` — user guide (DocBook XML) and release documentation.
- `PLAN.md`, `TAREAS.md`, `CONTEXTO.md`, `caracteristicas_futuras.md`,
  `preguntas.md` — project tracking files (see below).

## Branches

- `master` — the default branch on GitHub; what the community builds from.
- `develop` — the active development branch. **Target your pull requests at
  `master`** (the base branch used by this project's PR flow).
- Work on your own branch (e.g. `feature/xyz`, `fix/abc`) and open a PR when
  it is ready.

## Getting started

1. **Fork** the repository on GitHub, then clone your fork:
   ```bash
   git clone https://github.com/<your-user>/power-architect.git
   cd power-architect
   git remote add upstream https://github.com/SQLPower/power-architect.git
   ```
2. **Sync the vendored submodule** (required to build):
   ```bash
   git submodule update --init
   ```
3. Create a feature branch:
   ```bash
   git checkout -b feature/my-change develop
   ```

## Building and testing

All commands go through the Gradle wrapper (`./gradlew` on macOS/Linux,
`gradlew.bat` on Windows).

| Task | Purpose |
|------|---------|
| `./gradlew build` | Compile + assemble (includes annotation-processing passes) |
| `./gradlew run` | Compile + launch the Swing app |
| `./gradlew test` | Compile + run the test suites |
| `./gradlew compileExamples` | Validate code under `example_code/` |
| `./gradlew clean` | Remove build outputs |
| `./gradlew jpackageAppImage` | Native Windows app-image (64-bit, embedded JRE) |
| `./gradlew jpackageExe` | Native Windows installer (`.exe`, 64-bit); needs vendored WiX |
| `./gradlew jpackageMacDmg` / `jpackageLinuxDeb` / `jpackageLinuxRpm` | Cross-platform packages (run on the target OS) |

**Requirements:** JDK 21 toolchain (Gradle locates/installs it automatically)
and the `sqlpower-library` submodule initialized.

### Tests

- App suites: `ca.sqlpower.ArchitectBusinessTestSuite`,
  `ca.sqlpower.ArchitectSwingTestSuite`, `ca.sqlpower.ArchitectAutoTests`,
  `ca.sqlpower.architect.profile.ProfileTests`.
- The **Swing UI suite fails randomly** (known issue) — don't chase flaky
  failures there.
- Tests run headless. DB-touching tests use drivers from `jdbc_drivers/` and
  the HSQLDB test database from `testbed/hsqldb.jar`.
- **Before opening a PR**, run `./gradlew test` and report the result in the
  PR body (e.g. `App: 1189 tests / 2 known-flaky; Library: 973 / 2 pre-existing`).

## Coding conventions

See `DEVELOPERS` (top-level file) for the full list. Key rules:

- Sun-style conventions with the documented exceptions.
- `log4j` `Logger` field first, followed by a blank line.
- 4-space indentation using **spaces** (not tabs).
- No `Interface`/`I` prefix on interfaces (use `Foo` / `FooImpl`).
- Catch/event variables named `e`/`ex`.
- Single-line guard `if (cond) return;` is preferred; multi-line conditions
  require braces.

## Generated code — do not hand-edit

- **Annotation-processed persisters**: `SPAnnotationProcessor` generates
  `*SPPersisterHelper` classes in a second javac pass. After editing annotated
  model classes, re-run `./gradlew build`.
- **Mondrian OLAP classes**: XSLT-generated from
  `src/main/java/ca/sqlpower/architect/olap/Mondrian.xml` and checked in.
  Edit the XML (or the XSLT), then run:
  `./gradlew mondrianModel mondrianParser mondrianFormatter`.
- App version lives in `ca.sqlpower.architect.ArchitectVersion`, not a
  properties file.

## What not to break

- Keep `./gradlew test` green (or only the known-flaky failures).
- Do not commit IDE files (`.project`, `.classpath`, `.settings/`, `*.log`,
  `bin/`, `build/`, `dist/`) — they are ignored or must stay untracked.
- Do not bump the version in `ArchitectVersion`/`architect.version.properties`
  for feature work; releases are coordinated separately (see
  `doc/ReleaseNotes.txt`).
- If you change message bundles, add/update all supported languages
  (`messages_<lang>.properties`; see `SupportedLanguages`).

## Submitting a pull request

1. Commit your changes on your feature branch with clear, focused messages.
2. Push your branch to your fork:
   ```bash
   git push -u origin feature/my-change
   ```
3. On GitHub, open a pull request: base repository `SQLPower/power-architect`,
   base branch `master`, head `your-fork:feature/my-change`.
4. Fill out the PR template (title, summary, testing, checklist).
5. Keep the PR focused: one logical change per PR. Mention related issues.

## Project tracking files

These live in the repo and are kept up to date:

- `PLAN.md` — high-level execution plan and per-phase status.
- `TAREAS.md` — atomic tasks and their states.
- `CONTEXTO.md` — accumulated context, decisions, and technical specs.
- `caracteristicas_futuras.md` — backlog of not-yet-planned features.

When your change implements a planned task, update the corresponding entries
in these files as part of the PR.

## Getting help

- Ask questions on the `architect-developers` mailing list or open an issue
  on GitHub.
- For support, see the SQL Power forums: http://www.sqlpower.ca/forum/

This project is licensed under the GNU General Public License version 3
(see `LICENSE`).