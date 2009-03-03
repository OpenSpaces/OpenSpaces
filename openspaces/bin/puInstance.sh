#!/bin/bash

# The call to setenv.sh can be commented out if necessary.
. `dirname $0`/setenv.sh

echo Starting a Processing Unit Instance
if [ "${JSHOMEDIR}" = "" ] ; then 
  JSHOMEDIR=`dirname $0`/..
fi
export JSHOMEDIR

bootclasspath="-Xbootclasspath/p:$XML_JARS"

JAVACMD="${JAVA_HOME}/bin/java"


# Check for Cygwin
cygwin=
case $OS in 
    Windows*) 
        cygwin=1
esac
# For Cygwin, ensure paths are in UNIX format before anything is touched
if [ "$cygwin" = "1" ]; then
    CPS=";"
else
    CPS=":"
fi
export CPS

LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=${LOOKUPGROUPS}; export LOOKUP_GROUPS_PROP

if [ "${LOOKUPLOCATORS}" = "" ] ; then
LOOKUPLOCATORS=; export LOOKUPLOCATORS
fi
LOOKUP_LOCATORS_PROP="-Dcom.gs.jini_lus.locators=${LOOKUPLOCATORS}"; export LOOKUP_LOCATORS_PROP

COMMAND_LINE="${JAVACMD} ${JAVA_OPTIONS} $bootclasspath ${RMI_OPTIONS} ${LOOKUP_LOCATORS_PROP} ${LOOKUP_GROUPS_PROP} -Djava.security.policy=${POLICY} -Dcom.gs.home=${JSHOMEDIR} -classpath "${PRE_CLASSPATH}${CPS}${GS_JARS}${CPS}${SPRING_JARS}${CPS}${EXT_JARS}$CPS${JDBC_JARS}${CPS}${POST_CLASSPATH}" org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainer $*"

echo
echo
echo Starting puInstance with line:
echo ${COMMAND_LINE}

${COMMAND_LINE}
echo
echo
