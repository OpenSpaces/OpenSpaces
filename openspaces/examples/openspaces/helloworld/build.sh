JSHOMEDIR=`dirname $0`/../../..; export JSHOMEDIR
. ${JSHOMEDIR}/bin/setenv.sh

"$JAVACMD" ${LOOKUP_GROUPS_PROP} -classpath "${ANT_JARS}":"${JAVA_HOME}/lib/tools.jar" org.apache.tools.ant.Main $1
