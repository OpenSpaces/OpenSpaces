#!/bin/sh

# The call to setenv.sh can be commented out if necessary.
export JSHOMEDIR=`dirname $0`/../../
. `dirname $0`/../../bin/setenv.sh

if [ "${JSHOMEDIR}" = "" ] ; then
  JSHOMEDIR=`dirname $0`/..
fi
export JSHOMEDIR
export TMPDIR="/tmp"
export VERSION=`${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.OutputVersion`

${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.POMGenerator $TMPDIR $VERSION "${JSHOMEDIR}/tools/maven/maven-openspaces-plugin"

echo ""
echo ""
echo "Installing Version $VERSION"
echo ""
echo ""

# GigaSpaces Jars
mvn install:install-file -DgroupId=com.gigaspaces -DartifactId=gs-runtime -Dversion=$VERSION -DpomFile=$TMPDIR/gs-runtime-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/gigaspaces/gs-runtime.jar
mvn install:install-file -DgroupId=com.gigaspaces -DartifactId=gs-openspaces -Dversion=$VERSION -DpomFile=$TMPDIR/gs-openspaces-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/gigsspaces/gs-openspaces.jar
mvn install:install-file -DgroupId=com.gigaspaces -DartifactId=mule-os -Dversion=$VERSION -Dpackaging=jar -DpomFile=$TMPDIR/mule-os-pom.xml -Dfile=${JSHOMEDIR}/lib/opt/openspaces/mule-os.jar

# JMX Jars
mvn install:install-file -DgroupId=com.sun.jdmk -DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/platform/jmx/jmxtools.jar
mvn install:install-file -DgroupId=javax.management -DartifactId=jmxremote -Dversion=1.0.1_04 -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/platform/jmx/jmxremote.jar
mvn install:install-file -DgroupId=javax.management -DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/platform/jmx/jmxri.jar

# Build and install OpenSpaces Maven Plugin
mvn -f maven-openspaces-plugin/pom.xml install

# Copy licenese file
mvn os:install-license -Dfile=$JSHOMEDIR/gslicense.xml -Dversion=$VERSION

# Remove temp files
rm $TMPDIR/gs-runtime-pom.xml
rm $TMPDIR/gs-openspaces-pom.xml
rm $TMPDIR/mule-os-pom.xml
