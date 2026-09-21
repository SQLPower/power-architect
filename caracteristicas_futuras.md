# caracteristicas_futuras.md — Backlog de funcionalidades futuras

> Este archivo es un **backlog vivo**. Aquí viven las funcionalidades que aún **no** se
> están trabajando.
>
> Reglas:
> - Cuando una funcionalidad pasa a planificarse, **se elimina de este archivo** y se mueve
>   a `PLAN.md` / `TAREAS.md` / `CONTEXTO.md` según corresponda.
> - Cada entrada debe seguir la plantilla de abajo.
> - Prioridad sugerida: `ALTA` | `MEDIA` | `BAJA`.

## Plantilla de entrada

```
### [ ] <Nombre de la funcionalidad>
- **Prioridad:** ALTA | MEDIA | BAJA
- **Valor para el usuario:** <qué problema resuelve / qué mejora>
- **Referencia de mercado:** <producto(s) que lo ofrecen, ej. DbSchema, DataGrip, DBeaver>
- **Descripción funcional:** <comportamiento esperado>
- **Notas técnicas:** <módulos/clases afectados, dependencias, riesgos>
- **Criterios de aceptación:**
  - [ ] ...
- **Estado:** IDEA | EVALUANDO | APROBADA_PARA_PLAN
```

---

## Sugerencias alineadas con productos de mercado

Estas sugerencias ayudan a igualar la propuesta de valor de herramientas como DbSchema,
DBeaver, DataGrip, Navicat y ER/Studio. No están aprobadas: son candidatas.

### [ ] Documentación HTML5 exportable del esquema
- **Prioridad:** ALTA
- **Valor para el usuario:** Compartir el modelo con perfiles no técnicos sin instalar la app.
- **Referencia de mercado:** DbSchema (HTML5 documentation), ER/Studio.
- **Descripción funcional:** Generar un sitio HTML navegable con diagramas, tablas, columnas,
  claves y comentarios; buscable y con hover.
- **Notas técnicas:** Reutilizar `ddl/` + generación de reportes; empaquetar plantillas web.
- **Criterios de aceptación:**
  - [ ] Exporta un directorio HTML autocontenido.
  - [ ] Incluye diagrama y detalle por tabla.
- **Estado:** IDEA

### [ ] Scripts de migración / diff de esquema en SQL
- **Prioridad:** ALTA
- **Valor para el usuario:** Desplegar cambios revisables en lugar de DDL completo.
- **Referencia de mercado:** DbSchema (migration scripts, schema sync), Liquibase.
- **Descripción funcional:** Comparar dos versiones del modelo (o modelo vs. BD) y generar el
  script de migración en el dialecto destino.
- **Notas técnicas:** Ya existe el paquete `diff/`; combinar con `LiquibaseDDLGenerator`.
- **Criterios de aceptación:**
  - [ ] Genera ALTER/DROP/CREATE solo de diferencias.
- **Estado:** IDEA

### [ ] Editor SQL integrado con autocompletado del esquema
- **Prioridad:** MEDIA
- **Valor para el usuario:** Consultar y validar contra el modelo sin salir de la app.
- **Referencia de mercado:** DbSchema SQL editor, DataGrip, DBeaver.
- **Descripción funcional:** Editor con resaltado, autocompletado de tablas/columnas y
  ejecución de consultas con exportación de resultados (CSV/Excel/JSON).
- **Notas técnicas:** Ya hay `rsyntaxtextarea` vendido; evaluar actualizarlo.
- **Criterios de aceptación:**
  - [ ] Autocompleta nombres del modelo actual.
- **Estado:** IDEA

### [ ] Generación de datos de prueba realistas
- **Prioridad:** MEDIA
- **Valor para el usuario:** Poblar entornos de desarrollo/QA rápidamente.
- **Referencia de mercado:** DbSchema data generator, DBeaver.
- **Descripción funcional:** Reglas/patrones reutilizables por columna (nombres, emails,
  fechas, secuencias) para poblar tablas.
- **Notas técnicas:** Nuevo módulo `datagen/`.
- **Criterios de aceptación:**
  - [ ] Genera N filas respetando FKs y tipos.
- **Estado:** IDEA

### [ ] Versionado del modelo en Git
- **Prioridad:** MEDIA
- **Valor para el usuario:** Revisar cambios de esquema como código (branch/diff/PR).
- **Referencia de mercado:** DbSchema Git workflow.
- **Descripción funcional:** Formato de modelo diffeable + integración con Git.
- **Notas técnicas:** La persistencia actual es XML; evaluar formato estable/ordenado.
- **Criterios de aceptación:**
  - [ ] El modelo produce diffs legibles.
- **Estado:** IDEA

### [ ] Explorador de datos relacional (seguir FKs)
- **Prioridad:** MEDIA
- **Valor para el usuario:** Navegar registros relacionados sin escribir JOINs.
- **Referencia de mercado:** DbSchema relational data explorer, DBeaver.
- **Descripción funcional:** Ver/editar filas siguiendo relaciones FK, reales o virtuales.
- **Notas técnicas:** Reutilizar capa JDBC existente; UI de grilla.
- **Criterios de aceptación:**
  - [ ] Navega de una fila a sus relacionados.
- **Estado:** IDEA

### [ ] Asistente IA para consultas y diseño
- **Prioridad:** BAJA
- **Valor para el usuario:** Generar/explicar SQL en lenguaje natural sobre el propio modelo.
- **Referencia de mercado:** DbSchema AI Assistant.
- **Descripción funcional:** Pregunta en lenguaje natural → SQL, y explicación de SQL existente.
- **Notas técnicas:** Requiere integración con proveedor LLM y configuración de credenciales.
- **Criterios de aceptación:**
  - [ ] Usa el modelo actual como contexto.
- **Estado:** IDEA

### [ ] Tema visual configurable y atajos personalizables
- **Prioridad:** BAJA
- **Valor para el usuario:** Adaptar la herramienta al gusto/entorno del usuario.
- **Referencia de mercado:** DataGrip, DBeaver (keymaps y temas).
- **Descripción funcional:** Paletas adicionales (alto contraste), edición de keymap.
- **Notas técnicas:** Sobre FlatLaf; extiende la paleta de tema de la Fase 3.
- **Criterios de aceptación:**
  - [ ] Persiste tema y atajos por usuario.
- **Estado:** IDEA

### [ ] Exportación a PDF y Markdown de la documentación
- **Prioridad:** BAJA
- **Valor para el usuario:** Entregables formales del modelo.
- **Referencia de mercado:** DbSchema (PDF/Markdown docs).
- **Descripción funcional:** Exportar documentación del esquema a PDF/Markdown.
- **Notas técnicas:** iText ya está vendido; evaluar OpenPDF por licencia.
- **Criterios de aceptación:**
  - [ ] Genera PDF/Markdown con tablas y relaciones.
- **Estado:** IDEA


---

## Funcionalidades futuras del usuario

Estas funcionalidades son las deseadas por el usuario.

### [ ] Busqueda y Actulización dentro del mismo sistema
- **Prioridad:** MEDIA
- **Valor para el usuario:** El Sistema podra verificar la version actual y proponer actualizar al usuario a la nueva version, debe buscar el ultimo release en https://github.com/SQLPower/power-architect.
- **Referencia de mercado:** DBeaver.
- **Descripción funcional:** Al primer inicio de sesion del día consultar si hay un nuevo release del sistema en github, si es así lanzar un popup avisando de la nueva version y si se desea actualizar, saltar esta version o simplemente cancelar para seguir tal y como esta.
- **Notas técnicas:** La instalacion de la nueva version no debe romper la anterior ni modificar el lugar de instalacion original, debe ser lo mas transparente posible.
- **Criterios de aceptación:**
  - [ ] Verifica nueva version del sistema en repositorio.
  - [ ] Despliega un cuadro de texto que informe de la nueva version y de botones para elegir actulizarlo de inmediato y otro boton para cancelar o para saltarse la version.
- **Estado:** IDEA

### [ ] Traducción al español latino con selector de idioma
- **Prioridad:** MEDIA
- **Valor para el usuario:** Poder usar la aplicación en español latino, con opción de volver al inglés.
- **Referencia de mercado:** DbSchema, DBeaver, DataGrip (selectores de idioma).
- **Descripción funcional:** Localización completa de la interfaz al español latinoamericano (es_419 / es-LA), con un selector de idioma en las preferencias que permita volver al inglés (u otros idiomas existentes) sin reiniciar o con recarga de la UI.
- **Notas técnicas:** El proyecto ya usa bundles `messages*.properties` por paquete (existen `messages_de`, `messages_ko`, `messages_pt`, `messages_ru`). Añadir `messages_es.properties`/`messages_es_419.properties`, un selector en `PreferencesEditor` que cambie el `Locale` por defecto y re-cargue los bundles (limpiar caché de `Messages` y re-aplicar `SwingUtilities.updateComponentTreeUI`). Cubrir también los bundles de `swingui/action/`.
- **Criterios de aceptación:**
  - [ ] La UI se muestra en español latino al seleccionarlo.
  - [ ] El selector permite volver al inglés.
  - [ ] El cambio de idioma se persiste y aplica sin reiniciar.
- **Estado:** IDEA

### [ ] Distribución de 64 bits (x86_64)
- **Prioridad:** MEDIA
- **Valor para el usuario:** Instalador y aplicación nativos de 64 bits, aprovechando memoria y rendimiento de equipos modernos.
- **Referencia de mercado:** DbSchema, DBeaver (instaladores x64).
- **Descripción funcional:** Generar el instalador de Windows (y, en su caso, el launcher `architect.exe`) como binario **x86_64**, en lugar del x86 (32 bits) actual.
- **Notas técnicas:** launch4j 3.12 soporta `headerType`/bitness; revisar que el binario de salida sea de 64 bits y que la app (JVM 21) no dependa de componentes de 32 bits. IzPack es independiente del bitness (el instalador `.jar` corre en la JVM instalada), por lo que el cambio principal es el launcher y la validación del JRE de 64 bits. Verificar en la máquina destino que se instale/ejecute en un JRE x64.
- **Criterios de aceptación:**
  - [ ] El instalador/launcher generado es de 64 bits.
  - [ ] La app funciona en un JRE x86_64.
- **Estado:** IDEA

### [ ] Terminal integrada (panel inferior tipo VS Code) para agentes de IA
- **Prioridad:** MEDIA
- **Valor para el usuario:** Abrir una terminal posicionada en la carpeta del proyecto/archivo actual, y usarla para lanzar un agente de IA dentro de Power Architect.
- **Referencia de mercado:** VS Code (panel de terminal inferior), DbSchema (scripts de automatización).
- **Descripción funcional:** Un panel inferior (dock) con una terminal embebida que abre en el directorio del archivo de proyecto actual. Desde ahí el usuario puede ejecutar comandos (p. ej. lanzar un agente de IA de línea de comandos). Debe poder ocultarse/mostrarse, redimensionarse y arrancar con el directorio correcto.
- **Notas técnicas:** Opciones en Swing: `pty4j` (emulación de terminal nativa), JSch (SSH) o un `JTextArea`/`JEditorPane` para un pseudo-terminal simple que ejecute procesos con `ProcessBuilder` (directorio inicial = carpeta del archivo). Integrar como un `JPanel` en `ArchitectFrame` (JSplitPane inferior), con toggle por menú/atajo. Tener en cuenta el zoom de UI y el tema (FlatLaf) para el estilo de la terminal.
- **Criterios de aceptación:**
  - [ ] El panel inferior muestra una terminal funcional.
  - [ ] La terminal abre en el directorio del archivo/proyecto actual.
  - [ ] Se puede mostrar/ocultar y redimensionar.
- **Estado:** IDEA


---

## WISHLIST del creador (portada del archivo WISHLIST)

Esta sección porta las tareas técnicas concretas del archivo `WISHLIST` original del
proyecto (listado de deseos de los desarrolladores del código), actualizadas a la
modernización en curso (Gradle, JDK 21, GitHub, sin Ant). Se excluye la parte de
"bugs / comunidad" por no ser funcionalidades; los ítems de refactorización/código y
funciones nuevas se desglosan como tareas independientes, sin dependencias bloqueantes,
en el orden secuencial sugerido de desarrollo. Las referencias obsoletas del WISHLIST
(`build.xml`, repos Google Code, listas de correo) se reemplazan por sus equivalentes
actuales (`./gradlew pmd`/`pmdCpd`, GitHub).

### [ ] Reportes de calidad de código PMD y CPD en Gradle
- **Prioridad:** ALTA
- **Valor para el usuario:** Base medible de duplicación y problemas de programación para las tareas 2 y 3; reemplaza las referencias muertas del WISHLIST a `build.xml`.
- **Referencia de mercado:** N/A (herramientas de análisis estático PMD/CPD).
- **Descripción funcional:** Ejecutar `./gradlew pmd` y `./gradlew pmdCpd` (ya portados en `packaging.gradle`, PMD 6.29.0 vendido en `ext-tools-home/`) y documentar el estado inicial: duplicaciones CPD a refactorizar y hallazgos del ruleset básico de PMD a corregir.
- **Notas técnicas:** Sustituye a los targets `pmd` / `pmd-cpd` de `build.xml` (eliminado). Verificar que los reportes se generen correctamente en Windows (paths sin espacios). Los reportes son el insumo de las tareas siguientes, pero no las bloquean: cada una puede regenerar su propio reporte.
- **Criterios de aceptación:**
  - [ ] `./gradlew pmd` y `./gradlew pmdCpd` generan reportes sin errores de build.
  - [ ] Se documenta en CONTEXTO el número y tipo de hallazgos iniciales.
- **Estado:** IDEA

### [ ] Refactor de superclases comunes señaladas por CPD
- **Prioridad:** MEDIA
- **Valor para el usuario:** Eliminar código duplicado (copia-y-pega) que CPD detecta, facilitando el mantenimiento.
- **Referencia de mercado:** N/A (deuda técnica interna).
- **Descripción funcional:** Extraer superclases comunes en los puntos que el reporte CPD revela, eligiendo uno por uno los duplicados más grandes y verificando que no cambien el comportamiento.
- **Notas técnicas:** Reporte generado con `./gradlew pmdCpd`. Se puede abordar por grupos aislados (cada extracción es una tarea unitaria). Requiere entender las convenciones del repo (Sun-style, ver `DEVELOPERS`).
- **Criterios de aceptación:**
  - [ ] Cada duplicado refactorizado pasa la suite de tests correspondiente.
  - [ ] El número de duplicaciones reportadas por CPD baja en los módulos tocados.
- **Estado:** IDEA

### [ ] Corrección de hallazgos del ruleset básico de PMD
- **Prioridad:** MEDIA
- **Valor para el usuario:** Reducir bugs potenciales y malas prácticas de programación detectadas estáticamente.
- **Referencia de mercado:** N/A (deuda técnica interna).
- **Descripción funcional:** Tomar los hallazgos del ruleset básico de PMD y proponer/corregir cada uno, eligiendo por prioridad los de mayor riesgo.
- **Notas técnicas:** Reporte generado con `./gradlew pmd`. Cada corrección es independiente y debe validarse con la suite de tests; respetar las convenciones del repo.
- **Criterios de aceptación:**
  - [ ] Los hallazgos corregidos desaparecen del reporte PMD.
  - [ ] La suite de tests sigue en verde (app 1172/0; librería 971/2 pre-existentes documentados).
- **Estado:** IDEA

### [ ] Refactor del generador DDL (eliminar clases concretas por plataforma)
- **Prioridad:** ALTA
- **Valor para el usuario:** Facilitar soportar nuevas plataformas y mantener el DDL sin duplicar lógica por dialecto.
- **Referencia de mercado:** El WISHLIST original menciona el generador DDL del proyecto Jakarta (parte de DBUtils, Apache) como referencia a evaluar.
- **Descripción funcional:** Reemplazar el enfoque actual (una clase concreta por plataforma, con la mayoría del código como literales de String) por una configuración basada en datos/plantillas para los dialectos, conservando el API de `DDLGenerator` y la salida existente.
- **Notas técnicas:** `GenericDDLGenerator` (1520 líneas) es la base; clases por plataforma: `OracleDDLGenerator`, `MySqlDDLGenerator`, `SQLServerDDLGenerator`, `DB2DDLGenerator`, `H2DDLGenerator`, `HSQLDBDDLGenerator`, `PostgresDDLGenerator`, `LiquibaseDDLGenerator`. Evaluar Apache DBUtils / generadores de dialectos modernos antes de decidir el diseño. Riesgo alto: muchos tests dependen del DDL generado. Se recomienda hacerlo por fases, un dialecto a la vez.
- **Criterios de aceptación:**
  - [ ] El DDL generado por cada dialecto es idéntico al actual (diff de scripts de los tests).
  - [ ] Añadir un dialecto nuevo no requiere duplicar la clase completa.
- **Estado:** IDEA

### [ ] Algoritmos de layout de grafos avanzados para el canvas
- **Prioridad:** BAJA
- **Valor para el usuario:** Diagramas más legibles y auto-organizados para modelos grandes.
- **Referencia de mercado:** yEd, Graphviz (fuerza, capas/hierárquico, circular, ortogonal).
- **Descripción funcional:** Añadir más algoritmos de layout al paquete `layout/` (además de los existentes) y permitir seleccionarlos desde el menú de auto-layout.
- **Notas técnicas:** Ya existen `FruchtermanReingoldForceLayout`, `BasicTreeAutoLayout`, `ArchitectGridLayout`, `LineStraightenerLayout` y la interfaz `ArchitectLayout`. El WISHLIST original sugiere algoritmos de teoría de grafos más avanzados; evaluar literatura (fuerza dirigida mejorada, capas/Sugiyama) antes de implementar. Integración vía `AutoLayoutAction`/`LayoutAnimator`.
- **Criterios de aceptación:**
  - [ ] Al menos un algoritmo nuevo de layout es seleccionable y produce un diagrama válido.
  - [ ] No rompe el layout de fuerza existente ni los tests de `PlayPen`.
- **Estado:** IDEA

### [ ] Soporte de nuevas plataformas de base de datos
- **Prioridad:** MEDIA
- **Valor para el usuario:** Poder modelar/generar DDL en más motores de BD.
- **Referencia de mercado:** DbSchema, DataGrip (soporte multi-dialecto).
- **Descripción funcional:** Añadir soporte para plataformas de BD no cubiertas (o mejorar el existente), incluyendo generador DDL, mapeo de tipos y críticos asociados.
- **Notas técnicas:** Se beneficia directamente del refactor del generador DDL (va después en el orden sugerido, pero sin bloqueo: un dialecto nuevo puede implementarse sobre el esquema actual si se prefiere). Reutilizar `DDLUtils`, `TypeMap` y el patrón de críticos (`ddl/critic/`). Cada plataforma es una tarea unitaria independiente.
- **Criterios de aceptación:**
  - [ ] El nuevo dialecto genera DDL correcto para tablas, columnas, índices y FKs.
  - [ ] Incluye tests del generador y críticos de la plataforma.
- **Estado:** IDEA