#!/bin/bash

# The call to setenv.sh can be commented out if necessary.
export JSHOMEDIR=`dirname $0`/../../
. `dirname $0`/../../bin/setenv.sh

bootclasspath="-Xbootclasspath/p:$XML_JARS"

JAVACMD="${JAVA_HOME}/bin/java"

CPS=":"
export CPS

LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=${LOOKUPGROUPS}; export LOOKUP_GROUPS_PROP

if [ "${LOOKUPLOCATORS}" = "" ] ; then
LOOKUPLOCATORS=; export LOOKUPLOCATORS
fi
LOOKUP_LOCATORS_PROP="-Dcom.gs.jini_lus.locators=${LOOKUPLOCATORS}"; export LOOKUP_LOCATORS_PROP



VELOCITY_JARS="${JSHOMEDIR}"/lib/platform/velocity/*
export VELOCITY_JARS

COMMONS_JARS="${JSHOMEDIR}"/lib/platform/commons/*
export COMMONS_JARS

COMMAND_LINE="${JAVACMD} ${JAVA_OPTIONS} $bootclasspath -Dlb.vmDir="${JSHOMEDIR}/tools/apache" ${RMI_OPTIONS} ${LOOKUP_LOCATORS_PROP} ${LOOKUP_GROUPS_PROP} -Djava.security.policy=${POLICY} -Dcom.gs.home=${JSHOMEDIR} -classpath "${PRE_CLASSPATH}${CPS}${GS_JARS}${CPS}${SPRING_JARS}${CPS}${JDBC_JARS}${CPS}${VELOCITY_JARS}${CPS}${COMMONS_JARS}${CPS}${POST_CLASSPATH}" org.openspaces.pu.container.jee.lb.apache.ApacheLoadBalancerAgent $*"

echo
echo
echo Starting apache-lb-agent with line:
echo ${COMMAND_LINE}

${COMMAND_LINE}
echo
echo
