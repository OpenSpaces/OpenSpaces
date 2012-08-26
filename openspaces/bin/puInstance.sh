#!/bin/bash

# The call to setenv.sh can be commented out if necessary.
. `dirname $0`/setenv.sh

echo Starting a Processing Unit Instance
bootclasspath="-Xbootclasspath/p:$XML_JARS"

COMMAND_LINE="${JAVACMD} ${JAVA_OPTIONS} $bootclasspath ${GS_LOGGING_CONFIG_FILE_PROP} ${RMI_OPTIONS} ${LOOKUP_LOCATORS_PROP} ${LOOKUP_GROUPS_PROP} -Djava.security.policy=${POLICY} -Dcom.gs.home=${JSHOMEDIR} -classpath "${PRE_CLASSPATH}${CPS}${GS_JARS}${CPS}${SPRING_JARS}${CPS}${EXT_JARS}$CPS${JDBC_JARS}${CPS}${POST_CLASSPATH}" org.openspaces.pu.container.standalone.StandaloneProcessingUnitContainer $*"

echo
echo
echo Starting puInstance with line:
echo ${COMMAND_LINE}

${COMMAND_LINE}
echo
echo
