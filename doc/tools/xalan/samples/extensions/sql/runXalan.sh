#!/bin/sh
#
#=========================================================================
# Copyright 2004 The Apache Software Foundation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0

if [ "$DERBY_JAR_DIR" = "" ] ; then
    DERBYJAR_DIR=.
fi


_JAVACMD=$JAVA_HOME/bin/java


if [ "$JAVA_HOME" = "" ] ; then
    echo "Warning: JAVA_HOME environment variable is not set."
    _JAVACMD=java
fi



#TO Work with JDK 1.3 and JDK 1.4, we will keep the directory
#component seperate from the JAR name. This way we can use the
#directory component as part of the Endorsed Dir command line
#argument when running under JDK 1.4 this should also work for JDK 1.5

#The XML Apis are going to be either in the LIB dir for a source
#release or in the root direcctory for a binary release.


if [ -f ../../../build/xalan.jar ] ; then
echo "Configuring Xalan for a Source build"
_XALAN_DIR=../../../build/
else
echo "Configuring Xalan for a Release build"
_XALAN_DIR=../../../
fi

if [ -f ../../../lib/xercesImpl.jar ] ; then

_JAR_DIR=../../../lib/
else
_JAR_DIR=../../../
fi


if [ "$PARSER_JAR" = "" ] ; then
    PARSER_JAR=$_JAR_DIR/xercesImpl.jar
fi

if [ "$XML_APIS_JAR" = "" ] ; then
    XML_APIS_JAR=$_JAR_DIR/xml-apis.jar
fi

if [ "$XALAN_JAR" = "" ] ; then
    XALAN_JAR=$_XALAN_DIR/xalan.jar
    SERIALIZER_JAR=$_XALAN_DIR/serializer.jar 
fi



# Use _underscore prefix to not conflict with user's settings
# Default to UNIX-style pathing
CLPATHSEP=:
# if we're on a Windows box make it ;
uname | grep WIN && CLPATHSEP=\;

_CLASSPATH="$XALAN_JAR${CLPATHSEP}$XML_APIS_JAR${CLPATHSEP}$PARSER_JAR${CLPATHSEP}$XALAN_JAR${CLPATHSEP}$DERBYJAR_DIR/derby.jar${CLPATHSEP}$DERBYJAR_DIR/derbytools.jar${CLPATHSEP}$CLASSPATH"

# Attempt to automatically add system classes to _CLASSPATH
if [ -f $JAVA_HOME/lib/tools.jar ] ; then
  _CLASSPATH=${_CLASSPATH}${CLPATHSEP}${JAVA_HOME}/lib/tools.jar
fi

if [ -f $JAVA_HOME/lib/classes.zip ] ; then
  _CLASSPATH=${_CLASSPATH}${CLPATHSEP}${JAVA_HOME}/lib/classes.zip
fi

_ENDORSED_DIR=${_XALAN_DIR}${CLPATHSEP}${_JAR_DIR}

echo "Running Xalan: $@"
echo "...with classpath: $_CLASSPATH"

"$_JAVACMD" $JAVA_OPTS -Djava.endorsed.dirs=$_ENDORSED_DIR -classpath "$_CLASSPATH" -Dij.protocol=jdbc:derby: org.apache.xalan.xslt.Process $@




