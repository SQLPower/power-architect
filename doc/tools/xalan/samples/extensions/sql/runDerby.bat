@echo off
rem
rem ==========================================================================
rem = Copyright 2004 The Apache Software Foundation.
rem =
rem = Licensed under the Apache License, Version 2.0 (the "License");
rem = you may not use this file except in compliance with the License.
rem = You may obtain a copy of the License at
rem =
rem =     http://www.apache.org/licenses/LICENSE-2.0
rem =
rem = Unless required by applicable law or agreed to in writing, software
rem = distributed under the License is distributed on an "AS IS" BASIS,
rem = WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem = See the License for the specific language governing permissions and
rem = limitations under the License.
rem ==========================================================================
rem
rem     runDerby.bat: Script to execute the Derby tool IJ
rem     Usage: runDerby script file] 
rem     Setup:
rem         - you should set JAVA_HOME
rem         - you can set DERBY_HOME to point to both derby.jar and derbytools.jar
rem         - JAVA_OPTS is added to the java command line
rem	    - DERBY_JAR_DIR, default is the current directory

echo.

if not "%JAVA_HOME%" == "" goto setant
:noJavaHome
rem Default command used to call java.exe; hopefully it's on the path here
if "%_JAVACMD%" == "" set _JAVACMD=java
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If build fails because sun.* classes could not be found
echo   you will need to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.

:setant
rem Default command used to call java.exe or equivalent
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java


if "%DERBY_JAR_DIR%" == "" set DERBY_JAR_DIR=.

set _DERBY_JAR=%DERBY_JAR_DIR%\derby.jar;%DERBY_JAR_DIR%\derbytools.jar


rem Attempt to automatically add system classes to _CLASSPATH
rem Use _underscore prefix to not conflict with user's settings
set _CLASSPATH=%CLASSPATH%
if exist "%JAVA_HOME%\lib\tools.jar" set _CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar
if exist "%JAVA_HOME%\lib\classes.zip" set _CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\classes.zip
set _CLASSPATH=%_DERBY_JAR%;%_XML-APIS_JAR%;%_PARSER_JAR%;%_CLASSPATH%

echo %_CLASSPATH%

@echo on
"%_JAVACMD%" -mx64m %JAVA_OPTS% -classpath "%_CLASSPATH%" -Dij.protocol=jdbc:derby: org.apache.derby.tools.ij %1 %2
@echo off

goto end

:end
rem Cleanup environment variables
set _JAVACMD=
set _CLASSPATH=
set _ANT_HOME=
set _ANT_JAR=
set _PARSER_JAR=
set _XML-APIS_JAR=

