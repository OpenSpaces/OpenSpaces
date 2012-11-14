#!/bin/sh

# The call to setenv.sh can be commented out if necessary.
export JSHOMEDIR=`dirname $0`/../../
. `dirname $0`/../../bin/setenv.sh

if [ "${JSHOMEDIR}" = "" ] ; then
  JSHOMEDIR=`dirname $0`/..
fi
export JSHOMEDIR
export TMPDIR="/tmp"
export EDITION=`${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.OutputVersion edition`

export XAP_VERSION=`${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.OutputVersion XAP`

${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.POMGenerator $TMPDIR "${JSHOMEDIR}/tools/maven/maven-openspaces-plugin"

echo ""
echo ""
echo "Installing XAP $XAP_VERSION jars"
echo ""
echo ""

# GigaSpaces Jars
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-runtime -Dversion=$XAP_VERSION -DpomFile=$TMPDIR/gs-runtime-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/required/gs-runtime.jar -Djavadoc="${JSHOMEDIR}/docs/xap-javadoc.zip"
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-openspaces -Dversion=$XAP_VERSION -DpomFile=$TMPDIR/gs-openspaces-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/required/gs-openspaces.jar -Djavadoc="${JSHOMEDIR}/docs/xap-javadoc.zip" -Dsources="${JSHOMEDIR}/lib/optional/openspaces/gs-openspaces-sources.jar"
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=mule-os -Dversion=$XAP_VERSION -Dpackaging=jar -DpomFile=$TMPDIR/mule-os-pom.xml -Dfile=${JSHOMEDIR}/lib/optional/openspaces/mule-os.jar
mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=jetty-os -Dversion=$XAP_VERSION -Dpackaging=jar -DpomFile=$TMPDIR/jetty-os-pom.xml -Dfile=${JSHOMEDIR}/lib/platform/openspaces/gs-openspaces-jetty.jar

# Build and install OpenSpaces Maven Plugin
mvn -f maven-openspaces-plugin/pom.xml install -DcreateChecksum=true


if [ "${EDITION}" = "Cloudify" ] ; then
	export CLOUDIFY_VERSION=`${JAVACMD} -cp ${GS_JARS} org.openspaces.maven.support.OutputVersion Cloudify`
	
	echo ""
	echo ""
	echo "Installing Cloudify $CLOUDIFY_VERSION jars"
	echo ""
	echo ""
	
	mvn install:install-file -DgroupId=org.cloudifysource -DcreateChecksum=true -DartifactId=dsl -Dversion=$CLOUDIFY_VERSION -DpomFile=$TMPDIR/dsl-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/platform/cloudify/dsl.jar 
	mvn install:install-file -DgroupId=org.cloudifysource -DcreateChecksum=true -DartifactId=usm -Dversion=$CLOUDIFY_VERSION -DpomFile=$TMPDIR/usm-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/platform/usm/usm.jar
	mvn install:install-file -DgroupId=org.cloudifysource -DcreateChecksum=true -DartifactId=esc -Dversion=$CLOUDIFY_VERSION -DpomFile=$TMPDIR/esc-pom.xml -Dpackaging=jar -Dfile=${JSHOMEDIR}/lib/platform/esm/esc-$CLOUDIFY_VERSION.jar

fi

# Remove temp files
rm $TMPDIR/gs-runtime-pom.xml
rm $TMPDIR/gs-openspaces-pom.xml
rm $TMPDIR/mule-os-pom.xml
rm $TMPDIR/jetty-os-pom.xml

if [ "${EDITION}" = "Cloudify" ] ; then
	rm $TMPDIR/dsl-pom.xml
	rm $TMPDIR/usm-pom.xml

fi
