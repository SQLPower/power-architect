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
rem         - PARSER_JAR may be set to use alternate parser (default:..\..\..\bin\xercesImpl.jar)
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

rem Default command used to call java.exe or equivalent
if "%_JAVACCMD%" == "" set _JAVACCMD=%JAVA_HOME%\bin\javac


if "%DERBY_JAR_DIR%" == "" set DERBY_JAR_DIR=.
set _DERBY_JAR=%DERBY_JAR_DIR%\derby.jar;%DERBY_JAR_DIR%\derbytools.jar


REM TO Work with JDK 1.3 and JDK 1.4, we will keep the directory
REM component seperate from the JAR name. This way we can use the
REM directory component as part of the Endorsed Dir command line 
REM argument when running under JDK 1.4 this should also work for JDK 1.5

REM The XML Apis are going to be either in the LIB dir for a source
REM release or in the root direcctory for a binary release.

IF EXIST "..\..\..\lib\xercesImpl.jar" goto BIN_DIR_BINRELEASE

:BIN_DIR_SRCRELEASE
 Echo Setting bin JAR directory for a sOurce release
 set _BIN_JAR_DIR=..\..\..\
 goto REPORT_BIN_DIR

:BIN_DIR_BINRELEASE
 echo Setting bin JAR Directory for a binary release
 set _BIN_JAR_DIR=..\..\..\lib

:REPORT_BIN_DIR
Echo Binary JAR Directory: %_BIN_JAR_DIR%
echo .


REM The Parser JAR will be in the Lib Dir for the Source Release and for the
REM Binary Release they are in the root directory.

if exist  "..\..\..\build\xalan.jar" goto XALAN_DIR_BINRELEASE

:XALAN_DIR_SRCRELEASE
 Echo Configuring Xalan for a binary release
 set _XALAN_JAR_DIR=..\..\..\
 goto XALAN_DIR_REPORT

:XALAN_DIR_BINRELEASE  
 Echo Configuring Xalan for source release
 set _XALAN_JAR_DIR=..\..\..\build

:XALAN_DIR_REPORT
Echo Xalan Directory is: %_XALAN_JAR_DIR%
echo .


rem Attempt to automatically add system classes to _CLASSPATH
rem Use _underscore prefix to not conflict with user's settings

set _CLASSPATH=%CLASSPATH%
if exist "%JAVA_HOME%\lib\tools.jar" set _CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar
if exist "%JAVA_HOME%\lib\classes.zip" set _CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\classes.zip

set _CLASSPATH=%_XALAN_JAR_DIR%\xalan.jar;%_XALAN_JAR_DIR%\serializer.jar;%_BIN_JAR_DIR%\xml-apis.jar;%_BIN_JAR_DIR%\xercesImpl.jar;%_DERBY_JAR%;%_CLASSPATH%

set _CLASSPATH=ext-connection;%_CLASSPATH%

echo Using Classpath: %_CLASSPATH%
echo .

set _ENDORSED_DIR=%_XALAN_JAR_DIR%;%_BIN_JAR_DIR%
echo Setting Endorsed Dir to: %_ENDORSED_DIR% : Note ONLY USed for JDK 1.4
echo .

Echo Compiling the External connection Class
echo %_JAVACCMD% -d ./ext-connection -classpath "%_CLASSPATH%" ./ext-connection/ExternalConnection.java
%_JAVACCMD% -d ./ext-connection -classpath "%_CLASSPATH%" ./ext-connection/ExternalConnection.java

@echo on
"%_JAVACMD%" -mx64m %JAVA_OPTS% -Djava.endorsed.dirs=%_ENDORSED_DIR% -classpath "%_CLASSPATH%" ExternalConnection %1 %2 %3 %4 %5 %6 %7 %8
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


