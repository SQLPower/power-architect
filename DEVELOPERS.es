TODO mejorar este archivo. Terminar el esquema que se da al final de este documento.

Cómo Obtener el Código Fuente

Si usas Eclipse

Para obtener el código fuente de SQL Power Library y Power*Architect en un proyecto de Eclipse:

   1. Si aún no tienes la funcionalidad de Subversion disponible en tu versión de Eclipse, consigue Subclipse
   2. Inicia Eclipse y crea un proyecto nuevo (File -> New -> Project...)
   3. En la carpeta SVN, selecciona Checkout Projects from SVN. Si no ves la carpeta SVN, vuelve al paso 1. ;)
   4. Crea una ubicación de repositorio nueva para obtener SQL Power Library. Power*Architect requiere este proyecto para compilar. La URL del repositorio nuevo es http://sqlpower-library.googlecode.com/svn/
   5. Para obtener todos los archivos fuente más recientes de SQL Power Library, selecciona el directorio trunk y luego presiona Next
   6. Asegúrate de que las opciones "Check out as a project in the workspace" y "Head Revision" estén seleccionadas (son las opciones predeterminadas) y luego haz clic en Finish.
   7. Repite los pasos 2-6 para Power*Architect, excepto que en el paso 4 debes usar la siguiente URL de repositorio: http://power-architect.googlecode.com/svn/ 

Otros Entornos de Desarrollo

Actualmente, todos los integrantes del equipo de Architect hemos elegido usar Eclipse. Si tienes otro IDE de Java favorito, nos encantaría publicar aquí tus instrucciones para obtener el código de Architect. ¡Solo publica en la lista architect-developers!
svn de línea de comandos

Si solo quieres obtener el código fuente en un directorio local o tu entorno de desarrollo no se integra con Subversion, usa la herramienta svn de línea de comandos:

Para SQL Power Library:
	svn checkout http://sqlpower-library.googlecode.com/svn/trunk sqlpower-library 

Para Power*Architect:
    svn checkout http://power-architect.googlecode.com/svn/trunk power-architect 


Cómo Usar el Archivo de Compilación de Ant (build.xml)

Configuración de las propiedades de compilación:

	1. Crea una copia del archivo build.properties.example en el mismo directorio y cámbiale el nombre
a build.properties.
	2. Configura la propiedad sqlpower.library.home con el directorio donde tienes
	   el proyecto sqlpower-library. Power*Architect lo necesita para compilar. 
	3. Configura la propiedad build con el directorio donde te gustaría
que se compile architect. 

La siguiente sección asume que se usa Eclipse para el desarrollo.

Para compilar Power*Architect desde los archivos fuente se requiere Apache Ant versión 1.7 o superior. 
Consulta la siguiente subsección sobre cómo instalar Apache Ant versión 1.7 para 
Eclipse. Una vez que Ant 1.7 esté instalado, Power*Architect puede ejecutarse usando el
archivo build.xml. Para ejecutar Power*Architect abre el archivo build.xml y selecciona el
objetivo llamado run en el esquema. Haz clic derecho en el objetivo run y selecciona 
Ant Build en la opción del menú Run As. Otro objetivo interesante es el objetivo alltest.
Este objetivo ejecutará los diferentes conjuntos de pruebas disponibles con Power*Architect.
El objetivo alltest puede ejecutarse haciendo clic derecho en el objetivo alltest y seleccionando
Ant Build en la opción del menú Run As.

	Cómo Instalar Apache Ant Para Eclipse
	
	Apache Ant está disponible en http://ant.apache.org/bindownload.cgi. Para instalar Ant
	1.7 en Eclipse primero descomprime e instala Ant en tu máquina local. Luego, en
	las preferencias de usuario de Eclipse, selecciona el elemento Ant runtime en la lista de preferencias
	de la izquierda. Haz clic en el botón Ant Home y selecciona la carpeta en la que instalaste
	Ant 1.7. Luego haz clic en ok y Eclipse usará el nuevo Ant.


Suscríbete a las Listas de Correo de Power*Architect

Hay dos listas de correo para desarrolladores de Power*Architect, alojadas 
en Google Groups. La primera lista de correo es la lista de desarrolladores. La
lista de desarrolladores es para hacer preguntas, dar comentarios y sugerencias, o
cualquier otra inquietud que puedas tener mientras haces modificaciones. La lista de 
correo de desarrolladores está en http://groups.google.com/group/architect-developers. La 
segunda lista de correo es para confirmaciones (commits) de Power*Architect. La lista de 
correo de commits recibe un correo cada vez que se hace una confirmación al repositorio de Power*Architect 
con un comentario sobre lo que se cambió y una lista de las modificaciones. La 
lista de correo de commits está en http://groups.google.com/group/architect-commits.

Foro de Soporte Web

Para soporte adicional puedes visitar los foros de SQL Power en 
http://www.sqlpower.ca/forum/forums/list.page. 

Estándares de Codificación.


Usamos principalmente las convenciones de Sun para los estándares de codificación, descritas aquí:
http://java.sun.com/docs/codeconv/

Nuestras excepciones a las reglas de Sun, y algunas notas:

2.2 No usamos gnumake, así que ignora lo de GNUmakefile

3.1.1 El comentario introductorio que no es javadoc ahora debe ser la licencia BSD para 
	Architect y la biblioteca SQLPower de la que depende

3.1.3 (4) Siempre pon la declaración del Logger de log4j al principio, seguida de una línea 
	en blanco. No hace falta comentarla.

4. Nuestro tamaño de tabulación actual es 4, pero todos deberíamos configurar nuestros editores para usar 
	espacios en lugar de tabulaciones.  4 no es un tamaño de tabulación estándar (8 es el habitual), y 
	las tabulaciones de 4 frustrarán a personas externas que intenten trabajar con (o 
	simplemente leer) nuestro código. 

4.1 80 caracteres es una limitación un poco anticuada, pero las líneas excesivamente 
	largas son difíciles de leer.  Usa tu criterio aquí.

6.2 La guía de Sun recomienda declarar las variables solo al comienzo de un 
	bloque.  No estoy de acuerdo: el código es más manejable y mantenible cuando las variables 
	se declaran lo más tarde posible dentro de un bloque. 

6.3 Estoy de acuerdo en que debes inicializar una variable cuando se declara siempre 
	que sea posible.  Sin embargo, cuando hay lógica asociada a cómo inicializar 
	un valor, es mejor declararla y dejarla sin inicializar al principio, y luego, 
	en cada rama que siga, asignarle un valor a la variable.  De esta manera, 
	el compilador se asegurará de que todas las rutas posibles a través del código terminen 
	inicializando la variable. Ejemplo: 

	int foo;
	if (bar < 10) {
    	foo = 12;
	} else if (bar == 20) {
    	foo = 9;
	} else {
    	foo = bar * 3;
	}
	System.out.println(foo);    // Esta línea no compilará a menos que foo haya 
								// sido definitivamente inicializado (en este ejemplo, lo ha sido) 

7.4 La última parte muestra un ejemplo de código inaceptable:

	if (condition)
    	statement;

	También consideramos esto inaceptable.  Sin embargo, tenemos una construcción similar 
	que sí es aceptable: el modismo "guard" (guardia). 

	if (condition) return;

	o 

	if (condition) break;

	o

	if (condition) continue;

	Esto solo es aceptable (de hecho, es la forma preferida para expresar 
	el concepto de guardia) si la sentencia return, break o continue aparece en 
	la misma línea que el if.  Cuando la condición es lo bastante larga como para justificar un 
	salto de línea antes de la sentencia return, break o continue, debes usar llaves: 

	// MAL: el código ejecutado condicionalmente está en la siguiente línea pero no dentro de llaves
	if (condition || condition2 || condition3)
    	return;

	// CORRECTO: usa siempre llaves cuando el código ejecutado condicionalmente no esté 
	// en la misma línea que el if
	if (condition || condition2 || condition3) {
    	return;
	}


7.9 Nuestro nombre preferido para una variable de excepción en un bloque catch es "e" o 
	"ex", no "e1" como a Eclipse le gusta autogenerar.

	Nuestro nombre preferido para un objeto de evento en un manejador de eventos también es "e".  
	Por lo tanto, a menudo verás que los bloques catch nombran sus excepciones "ex" 
	en el código relacionado con Swing para evitar confusión y conflictos de nombres. 

9. (Nombrado de interfaces) Además, evita la tentación de usar la palabra 
	"Interface" en el nombre de una interfaz, y la tentación similar de comenzar 
	el nombre de una interfaz con una I.  A los nombres de las interfaces se les deben dar los nombres más limpios 
	y naturales posibles.  Por ejemplo, si estás creando una interfaz 
	y una implementación para un aparato que frota bebidas, deberías nombrar la 
	interfaz BeverageFrobber y la clase de implementación BeverageFrobberImpl.  
	Si habrá varias implementaciones que se beneficiarían de extender 
	una implementación base abstracta, aún crearías una interfaz BeverageFrobber, 
	luego crearías una clase abstracta AbstractBeverageFrobber y la extenderías 
	a las clases concretas nombradas según su propósito particular 
	(BlendingBeverageFrobber, SippingBeverageFrobber, PouringBeverageFrobber, y así sucesivamente). 
		
La Lista de Deseos

Para los desarrolladores nuevos en Power*Architect que quieran empezar a hacer contribuciones a 
Power*Architect, hay una lista de deseos incluida con el código fuente que los desarrolladores
han armado.


Esquema:
-cómo obtener los códigos fuente
-¡suscríbete a las listas de correo!
 -lista de registro de commits
 -lista de discusión de desarrolladores
 -foro de soporte web
-aprende los estándares de codificación
-cómo usar el archivo de compilación
 -cómo actualizar Ant a 1.7+ en Eclipse
-lee la WISHLIST (lista de deseos)
-cómo crear un parche
-cómo enviar un parche
-cómo convertirte en committer