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

# Jini Jars
mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-start -Dversion=$VERSION -DpomFile=$TMPDIR/jini-start-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/jini/start.jar
mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-jsk-lib -Dversion=$VERSION -DpomFile=$TMPDIR/jini-jsk-lib-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/jini/jsk-lib.jar
mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-jsk-platform -Dversion=$VERSION -DpomFile=$TMPDIR/jini-jsk-platform-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/jini/jsk-platform.jar
mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-jsk-resources -Dversion=$VERSION -DpomFile=$TMPDIR/jini-jsk-resources-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/jini/jsk-resources.jar
mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-reggie -Dversion=$VERSION -DpomFile=$TMPDIR/jini-reggie-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/jini/reggie.jar
mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-mahalo -Dversion=$VERSION -DpomFile=$TMPDIR/jini-mahalo-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/jini/mahalo.jar

# GigaSpaces Jars
mvn install:install-file -DgroupId=com.gigaspaces.core -DartifactId=gs-boot -Dversion=$VERSION -DpomFile=$TMPDIR/gs-boot-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/ServiceGrid/gs-boot.jar
mvn install:install-file -DgroupId=com.gigaspaces.core -DartifactId=gs-service -Dversion=$VERSION -DpomFile=$TMPDIR/gs-service-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/ServiceGrid/gs-service.jar
mvn install:install-file -DgroupId=com.gigaspaces.core -DartifactId=gs-lib -Dversion=$VERSION -DpomFile=$TMPDIR/gs-lib-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/ServiceGrid/gs-lib.jar
mvn install:install-file -DgroupId=com.gigaspaces.core -DartifactId=JSpaces -Dversion=$VERSION -DpomFile=$TMPDIR/JSpaces-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/JSpaces.jar
mvn install:install-file -DgroupId=org.openspaces -DartifactId=openspaces -Dversion=$VERSION -DpomFile=$TMPDIR/openspaces-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/openspaces/openspaces.jar
mvn install:install-file -DgroupId=org.openspaces -DartifactId=mule-os -Dversion=$VERSION -Dpackaging=jar -DpomFile=$TMPDIR/mule-os-pom.xml -Dfile=${JSHOMEDIR}/lib/openspaces/mule-os.jar

# JMX Jars
mvn install:install-file -DgroupId=com.sun.jdmk -DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/jmx/jmxtools.jar
mvn install:install-file -DgroupId=javax.management -DartifactId=jmxremote -Dversion=1.0.1_04 -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/jmx/jmxremote.jar
mvn install:install-file -DgroupId=javax.management -DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/jmx/jmxri.jar

# Build and install OpenSpaces Maven Plugin
mvn -f maven-openspaces-plugin/pom.xml install

# Copy licenese file
mvn os:install-license -Dfile=$JSHOMEDIR/gslicense.xml -Dversion=$VERSION

# Remove temp files
rm $TMPDIR/jini-start-pom.xml
rm $TMPDIR/jini-jsk-lib-pom.xml
rm $TMPDIR/jini-jsk-platform-pom.xml
rm $TMPDIR/jini-jsk-resources-pom.xml
rm $TMPDIR/jini-reggie-pom.xml
rm $TMPDIR/jini-mahalo-pom.xml
rm $TMPDIR/gs-boot-pom.xml
rm $TMPDIR/gs-service-pom.xml
rm $TMPDIR/gs-lib-pom.xml
rm $TMPDIR/JSpaces-pom.xml
rm $TMPDIR/openspaces-pom.xml
rm $TMPDIR/mule-os-pom.xml
