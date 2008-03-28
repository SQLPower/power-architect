REM Launches the architect using the newest available Java JDK Thingy
${INSTALL_DISK}
cd ${INSTALL_PATH}
"C:\Program Files\JavaSoft\JDK-1.5.0_06\bin\javaw" -Xmx600M -Dlog4j.configuration=file:log4j.properties -jar architect.jar
