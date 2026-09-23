# Diseño: Localización multi-idioma (ES, EN, PT, ZH, JA, DE)

Fecha: 2026-09-21
Estado: APROBADO (diseño validado con el usuario)

## Objetivo

Soportar la interfaz de Power*Architect en **español, inglés, portugués, chino,
japonés y alemán**, mediante archivos de traducción separados por idioma usando el
estándar Java `ResourceBundle`. El idioma se selecciona en las preferencias de la
app y, además, en el proceso de instalación (izpack) para que el instalador se
muestre en el idioma elegido.

## Decisiones clave (con el usuario)

| Tema | Decisión |
|------|----------|
| Alcance de traducción | Toda la UI: los 6 paquetes con bundle base (~840 claves) |
| Selector de idioma | Preferences (ya existe `LocaleChooser`) + selector en el instalador izpack |
| Cambio de idioma en la app | Requiere reinicio (comportamiento actual: persiste y aplica al arrancar) |
| Contenido de traducciones | Se generan las traducciones nuevas (es, zh, ja + completar de/pt) como `messages_<lang>.properties` |
| Estándar | `ResourceBundle` de Java (convención `messages*.properties`) — patrón ya usado en el proyecto |

## Estado actual (explorado)

- Bundles base por paquete (Java `ResourceBundle`):
  - App: `ca/sqlpower/architect/`, `ca/sqlpower/architect/swingui/`,
    `ca/sqlpower/architect/swingui/action/` (462 + 254 + 9 claves).
  - Librería: `ca/sqlpower/swingui/`, `ca/sqlpower/swingui/query/`,
    `ca/sqlpower/sqlobject/` (82 + 30 + 7 claves).
- Idiomas existentes: `en` (default), `de`, `ko`, `pt`, `ru`. Faltan `es`, `zh`, `ja`.
- `Messages` (app y librería) resuelven con `ResourceBundle.getBundle(bundle, Locale.getDefault())`
  de forma estática; el idioma se lee al arrancar.
- `LocaleChooser` (`ca/sqlpower/architect/LocaleChooser.java`) lista
  `en, de, ko, ru, pt`.
- `ArchitectFrame` aplica `Locale.setDefault` desde
  `ArchitectSwingUserSettings.DEFAULT_LOCALE`.
- Instalador izpack: `build.gradle` genera `install.xml` con un único
  `<langpack iso3="eng"/>`. El izpack 4.3.1 vendido incluye langpacks para
  eng, spa, por, chn, jpn, deu (confirmado en `standalone-compiler.jar`).

## Diseño

### 1. Bundles de traducción (ResourceBundle estándar)

Para cada paquete con `messages.properties` base, crear:
- `messages_es.properties`
- `messages_zh.properties`
- `messages_ja.properties`

Y completar las claves faltantes (contra el base) en:
- `messages_de.properties` (existe, puede faltar claves nuevas)
- `messages_pt.properties` (existe, puede faltar claves nuevas)

`ko` y `ru` quedan como están (ya existen); el fallback del `ResourceBundle` usa el
idioma más cercano o el base (inglés) si falta una clave.

Paquetes a cubrir (6):
1. App: `ca/sqlpower/architect/`
2. App: `ca/sqlpower/architect/swingui/`
3. App: `ca/sqlpower/architect/swingui/action/`
4. Librería: `ca/sqlpower/swingui/`
5. Librería: `ca/sqlpower/swingui/query/`
6. Librería: `ca/sqlpower/sqlobject/`

### 2. Selector en Preferences

- `LocaleChooser.availableLocales`: añadir `new Locale("es")`, `new Locale("zh")`,
  `new Locale("ja")`. El selector persiste `DEFAULT_LOCALE` (sin cambios en
  `PreferencesPanel`/`ArchitectFrame`).

### 3. Instalador izpack (selector de idioma + proceso en el idioma elegido)

- En `build.gradle`, el `install.xml` generado pasa a declarar múltiples langpacks:
  ```xml
  <locale>
    <langpack iso3="eng"/>
    <langpack iso3="spa"/>
    <langpack iso3="por"/>
    <langpack iso3="chn"/>
    <langpack iso3="jpn"/>
    <langpack iso3="deu"/>
  </locale>
  ```
- izpack muestra automáticamente un selector de idioma al inicio cuando hay más de
  un langpack; los paneles/botones del instalador se muestran en el idioma elegido.

### 4. Persistencia / arranque

- `ArchitectFrame` ya aplica el locale desde prefs; no requiere cambios.
- El cambio en Preferences aplica al reiniciar la app.

## Archivos afectados

- Nuevos: `messages_es/zh/ja.properties` × 6 paquetes (18 archivos), más completar
  `_de`/`_pt` (hasta 12 archivos editados).
- `LocaleChooser.java`: añadir 3 idiomas.
- `build.gradle`: `install.xml` con 6 langpacks.
- Tests: carga de bundles por idioma.

## Testing

- Test unitario que verifique que cada `messages_<lang>.properties` (es, pt, zh, ja,
  de) carga con `ResourceBundle.getBundle(...)` y no arroja `MissingResourceException`
  para un conjunto de claves representativas (o que el conteo de claves coincide con
  el base).
- Verificación manual: el selector de Preferences lista los 6 idiomas; el instalador
  muestra el selector de idioma al inicio y se instala en el idioma elegido.

## Fuera de alcance (YAGNI)

- Cambio de idioma en caliente (sin reiniciar): descartado (requiere reinicio).
- Selector en primer arranque (welcome): descartado.
- Traducción de `ko`/`ru` adicional (ya existen): fuera de alcance salvo completar
  claves nuevas si se detectan faltantes (opcional).