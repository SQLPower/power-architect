REM This is a temporary way to launch the architect.  We will migrate to WebStart ASAP!
set CLASSPATH=build;lib\commons-beanutils.jar;lib\commons-collections-3.0.jar;lib\commons-digester.jar;lib\commons-logging.jar;lib\jlfgr-1_0.jar;lib\junit.jar;lib\log4j.jar;lib\sqlpower.jar;drivers\classes12.jar;drivers\mssqlserver.jar;drivers\mssutil.jar;drivers\msbase.jar
java -Dlog4j.configuration=file:log4j.properties ca.sqlpower.architect.swingui.ArchitectFrame
