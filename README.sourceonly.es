Este README hace referencia al paquete de solo código fuente (source-only) de Power*Architect.

El paquete de solo código fuente está pensado como una distribución mínima,
destinada únicamente a sistemas de empaquetado automático que gestionan sus
propias dependencias de librerías.

Si buscas el código fuente con las dependencias incluidas, puedes obtener una
copia del proyecto desde GitHub. Los repositorios oficiales son:

- Power*Architect:  https://github.com/SQLPower/power-architect
- SQL Power Library: https://github.com/SQLPower/sqlpower-library

La SQL Power Library es necesaria para compilar Power*Architect y está incluida
como un submódulo de git en `sqlpower-library/`. Después de clonar, ejecuta:

    git submodule update --init

Requisitos:

- JDK 21 (Gradle localiza automáticamente un toolchain de JDK 21)
- Gradle (wrapper incluido)

Compilar y ejecutar:

    ./gradlew build      # compilar + ensamblar (incluye el procesamiento de anotaciones)
    ./gradlew run        # compilar + lanzar la aplicación Swing
    ./gradlew test       # compilar + ejecutar las suites de pruebas

Generar el instalador de Windows:

    ./gradlew buildWindowsInstaller

Produce `dist/SQL-Power-Architect-Setup-Windows-<version>.exe`.
El compilador de izpack debe ejecutarse en un JDK 11 o inferior (se puede
sobrescribir con `-PizpackJavaHome=<home del JDK 11>`); launch4j corre en JDK 21.

Consulta `AGENTS.md` para los prerequisitos de compilación detallados, comandos
y convenciones del proyecto, y `CHANGELOG.md` para el historial de cambios.

Para soporte adicional, visita los foros de SQL Power en
http://www.sqlpower.ca/forum/forums/list.page

Este proyecto está licenciado bajo la GNU General Public License versión 3
(consulta el archivo LICENSE).