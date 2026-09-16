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