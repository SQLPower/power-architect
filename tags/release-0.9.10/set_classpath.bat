@echo off
set CLASSPATH=build

for %%f in (lib\*.jar) do call add_to_classpath.bat %%f
for %%f in (p:\jonathan\jdbc\*.jar) do call add_to_classpath.bat %%f

echo %CLASSPATH%
