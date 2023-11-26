#!/bin/sh
#
#Generate the pom.xml and install-mvn-deps.sh files.
#The install-mvn-deps.sh installs dependencies in the local
#maven repository.

SCRIPT=`readlink -f "$0"`
SCRIPTD=${SCRIPT%/*}

POM="pom.xml"
DEP_SCRIPT="install-mvn-deps.sh"

if [ `pwd` != "${SCRIPTD}" ]; then
  echo "Script execution must be from the project directory $SCRIPTD"
  return
fi

if [ ! -d ./lib/power-lib ]; then
  echo "sqlpower-library libs do not not exist."
  echo "copy/link sqlpower-library/lib/main to power-architect/lib/power-lib"
  echo "copy/link sqlpower-library/dist/sqlpower_library.jar to power-architect/lib/power-lib"
fi

rm -f "$DEP_SCRIPT"
rm -f "$POM"

touch $DEP_SCRIPT
touch $POM
chmod +x $DEP_SCRIPT

cat <<OUT >> $POM

<?xml version="1.0" ?>
<project>

  <modelVersion>4.0.0</modelVersion>
  <groupId>com.intellires</groupId>
  <artifactId>power-architect</artifactId>
  <version>1.0</version>
  <name>PowerArchitect</name>
  <packaging>jar</packaging>

  <properties>
   <maven.compiler.source>11</maven.compiler.source>
   <maven.compiler.target>11</maven.compiler.target>
 </properties>

 <dependencies>
OUT

for file in `ls ./lib/*.jar ./lib/power-lib/*.jar`
do
  FILENAME=${file##*/}
  NAME=${FILENAME%.*}

  ALREADY_DONE=`cat "$DEP_SCRIPT" |grep -c $NAME`

  if [ $ALREADY_DONE -eq 0 ]; then
    echo "  <dependency>" >> $POM
    echo "    <groupId>pwr-arch-local-jar</groupId>" >> $POM
    echo "    <artifactId>"${NAME}"</artifactId>" >> $POM
    echo "    <version>1</version>" >> $POM
    echo "  </dependency>" >> $POM

    echo "mvn install:install-file -Dfile="$file" -DgroupId=pwr-arch-local-jar  -DartifactId="${NAME}" -Dversion=1 -Dpackaging=jar" >> ./install-mvn-deps.sh >> $DEP_SCRIPT
  fi
done;

ALREADY_DONE=`cat "$DEP_SCRIPT" |grep -c sqlpower_library.jar`
if [ $ALREADY_DONE -eq 0 ]; then
  echo "copy/link sqlpower-library/dist/sqlpower_library.jar to power-architect/lib/power-lib"
fi

cat <<OUT >> $POM
 </dependencies>
 <build>
  <plugins>
   <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-assembly-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
     <descriptorRefs>
      <descriptorRef>jar-with-dependencies</descriptorRef>
     </descriptorRefs>
     <archive>
      <manifest>
         <mainClass>ca.sqlpower.architect.swingui.ArchitectFrame</mainClass>
      </manifest>
     </archive>
    </configuration>
    <executions>
     <execution>
      <phase>package</phase>
      <goals>
       <goal>single</goal>
      </goals>
     </execution>
    </executions>
   </plugin>

   <plugin>
     <groupId>org.codehaus.mojo</groupId>
     <artifactId>exec-maven-plugin</artifactId>
     <version>3.1.1</version>
     <configuration>
       <executable>java</executable>
       <!-- optional -->
       <workingDirectory>/tmp</workingDirectory>
       <arguments>
         <argument>-classpath</argument>
         <classpath/>
         <argument>ca.sqlpower.architect.swingui.ArchitectFrame</argument>
       </arguments>
       <!--environmentVariables>
         <LANG>en_US</LANG>
       </environmentVariables-->
     </configuration>
   </plugin>
  </plugins>
 </build>
</project>
OUT

