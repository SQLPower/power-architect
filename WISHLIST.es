Lista de Deseos de Desarrollo de Architect

$Id$

¿Te interesa empezar a colaborar con el desarrollo de
Power*Architect, pero no sabes por dónde comenzar?

Nos alegra que lo preguntes. :)

Esta es una lista de deseos de cosas que nos encantaría
hacer nosotros mismos, pero para las que (¿todavía?) no
hemos encontrado tiempo. Si tienes ganas de ensuciarte las
manos, échale un vistazo a esta lista. Si se te ocurre una
solución, simplemente hazla realidad y publica un parche en
la lista de correo de desarrolladores. Si algo de esta lista
te llama la atención, pero no sabes por dónde empezar,
publica una nota en la lista de correo y alguien con gusto te
señalará la dirección correcta.

Antes de abordar cualquier cosa de esta lista, por favor lee
el archivo DEVELOPERS, que incluye instrucciones importantes
sobre cómo obtener la copia más reciente del código desde
nuestro repositorio de código fuente, cómo usar el sistema
de compilación de Architect y otra información para arrancar
el desarrollo.

Ahora, vamos a la lista:


Limpieza de Código y Refactorización

* CPD (el detector de copiar-y-pegar) revela varios lugares
  que necesitan extraer una superclase común. Ejecuta el
  objetivo pmd-cpd en build.xml para obtener un reporte,
  ¡y luego elige uno y hazlo realidad!

* El conjunto de reglas básico de PMD señala varios problemas
  potenciales de programación en Architect. Elige tu favorito
  y propón una solución en la lista de desarrolladores.
  Ejecuta el objetivo pmd en build.xml para generar una copia
  actualizada del reporte.

* El paquete generador de DDL necesita a gritos una
  refactorización. El enfoque actual requiere una clase
  concreta por cada plataforma de base de datos soportada, y
  la mayor parte del código en cada clase son solo literales
  de cadena. Otra cosa tentadora de revisar es el generador
  de DDL del proyecto Jakarta (parte de DBUtils, creo). Si te
  interesa abordar este punto, publica en la lista y
  diseñaremos un plan.

* Por muy divertido que sea el diseño spring en Architect (y
  ciertamente no querríamos deshacernos de él por completo..
  BOOOIINNG) hay otros algoritmos de diseño más avanzados que
  también nos gustaría soportar. Si amas la teoría de grafos
  y las matemáticas duras en general, mándanos una nota.
  Incluso conocemos algunos artículos que podemos señalarte
  como punto de partida. :)


Bugs, bugs, bugs

* Aunque no estés dispuesto a aportar código (o simplemente
  no tengas tiempo), los reportes de bugs de alta calidad son
  una de las mejores contribuciones que puedes hacer al
  proyecto. ¿Qué hace que un reporte de bug sea de alta
  calidad? En resumen, los mejores reportes de bugs implican
  un conjunto de instrucciones simple y detallado que
  cualquiera pueda seguir para reproducir un bug de manera
  confiable. Un reporte de este tipo incluirá necesariamente
  información como la versión específica de Architect contra
  la que se reporta el bug, el proveedor y número de versión
  de tu JVM, y la versión de tu sistema operativo.

* Para los que estén dispuestos a meterse de lleno y aportar
  código, no falta material en nuestro repositorio de bugs.
  Échale un vistazo y mira si hay algo que te interese
  abordar. Para los bugs, la mejor manera de empezar es crear
  un caso de prueba nuevo que exponga el bug y publicarlo en
  la lista de desarrolladores.

* Las pruebas ad-hoc también son una parte realmente
  importante del control de calidad. Si crees que no hay nada
  interesante en la base de datos de bugs actual, por favor
  dinos qué estás fumando y dónde conseguirlo. Luego, siempre
  puedes intentar encontrar problemas nuevos usando la
  aplicación tú mismo.


Funciones Nuevas

* Siempre nos interesa soportar nuevas plataformas de bases
  de datos y mejorar el soporte de las que ya manejamos. Si
  el soporte de Architect para tu base de datos favorita no
  está a la altura (o simplemente no existe...), podemos
  mostrarte cómo sumarte y hacer las cosas mejor.

* ¿Hay algo que te gustaría que Architect hiciera y que aún
  no hace? Menciónalo en la lista de correo de
  desarrolladores y con gusto lo discutiremos contigo.