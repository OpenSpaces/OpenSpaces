#!/bin/sh

# The call to setenv.sh can be commented out if necessary.
. `dirname $0`/setenv.sh

echo Starting a Processing Unit Instance
if [ "${JSHOMEDIR}" = "" ] ; then 
  JSHOMEDIR=`dirname $0`/..
fi
export JSHOMEDIR

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

for i in ${JSHOMEDIR}/lib/openspaces/*.jar
do
    OPENSPACES_JARS=${UI_JARS}$CPS$i
done
export OPENSPACES_JARS

LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=${LOOKUPGROUPS}; export LOOKUP_GROUPS_PROP

if [ "${LOOKUPLOCATORS}" = "" ] ; then
LOOKUPLOCATORS=""; export LOOKUPLOCATORS
fi
LOOKUP_LOCATORS_PROP="-Dcom.gs.jini_lus.locators=${LOOKUPLOCATORS}"; export LOOKUP_LOCATORS_PROP

COMMAND_LINE="${JAVACMD} ${JAVA_OPTIONS} ${RMI_OPTIONS} ${LOOKUP_LOCATORS_PROP} ${LOOKUP_GROUPS_PROP} -Djava.security.policy=${POLICY} -Dcom.gs.home=${JSHOMEDIR} -classpath "${COMMON_JARS}${CPS}${SPRING_JARS}${CPS}${EXT_JARS}$CPS${JSHOMEDIR}${CPS}${JSHOMEDIR}/lib/JSpaces.jar$CPS${OPENSPACES_JARS}" org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainer $1 $2 $3 $4 $5 $6 $7 $8 $9"

echo
echo
echo Starting puInstance with line:
echo ${COMMAND_LINE}

${COMMAND_LINE}
echo
echo
