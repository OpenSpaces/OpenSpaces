#!/bin/sh

#  *************************************************************************
#  This script is used to initialize common environment to GigaSpaces Server.
# 
#  It sets the following variables:
# 
#  JAVA_HOME  - Location of the JDK version used to start GigaSpaces
#               Server.
#				Note that YOU MUST SUPPLY A JAVA_HOME environment variable.
#  JAVACMD - The Java command-line
#   
#  JAVA_OPTIONS   - Java command-line options for running the server,
#             Including: The Java args to override the standard memory arguments passed to java,
#             - Arg specifying the JVM to run.  (i.e. -server, -hotspot, -jrocket etc.)
#             - GC, profiling and management options.
#  JAVA_VENDOR
#             - Vendor of the JVM (i.e. All, BEA, HP, IBM, Sun, etc.)
#			  - Default is ALL, meaning general settings
#  RMI_OPTIONS
#             - Additional RMI optional properties.  
#  JAVA_VM - The java arg specifying the JVM to run.  (i.e. 
#               -server, -hotspot, -jrocket etc.)
#  JSHOMEDIR  - The GigaSpaces home directory.
#  POLICY 	  - The default security policy file.
#  PRODUCTION_MODE
#             - Indicates if GigaSpaces Server will be started in Production
#               mode (default to the production mode).
#  LOOKUPGROUPS - Jini Lookup Service Group
#
#  LOOKUPLOCATORS - Jini Lookup Service Locators used for unicast discovery
#  
#  NIC_ADDR 	- The Network Interface card IP Address
# 
#   For additional information, refer to the GigaSpaces OnLine Documentation
#   at http://www.gigaspaces.com/docs.htm
#  *************************************************************************

# Set VERBOSE=true for debugging output
if [ "${VERBOSE}" = "" ] ; then 
  VERBOSE=false
fi

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

# - Set or override the JAVA_HOME variable
# - By default, the system value is used.
# JAVA_HOME="/utils/jdk1.5.0_01"
# - Reset JAVA_HOME unless JAVA_HOME is pre-defined.
if [ -z "${JAVA_HOME}" ]; then
  	echo "The JAVA_HOME environment variable is not set. Using the java that is set in system path."
  	JAVACMD=java; export JAVACMD
	JAVACCMD=javac; export JAVACCMD
	JAVAWCMD=javaw; export JAVAWCMD
else
  	echo JAVA_HOME environment variable is set to ${JAVA_HOME} in "<GigaSpaces Root>\bin\setenv.sh"
  	JAVACMD="${JAVA_HOME}/bin/java"; export JAVACMD
	JAVACCMD="${JAVA_HOME}/bin/javac"; export JAVACCMD
	JAVAWCMD="${JAVA_HOME}/bin/javaw"; export JAVAWCMD
fi

# Reset JAVA_VENDOR and PRODUCTION_MODE unless PRODUCTION_MODE
# AND JAVA_VENDOR are pre-defined.
if [ -z "${PRODUCTION_MODE}" -o -z "${JAVA_VENDOR}" ]; then
  # Set up JAVA VENDOR, possible values are
  #ALL, BEA, HP, IBM, Sun ...
  JAVA_VENDOR=ALL
  # PRODUCTION_MODE, default to the production mode
  PRODUCTION_MODE=true
  # echo JAVA_VENDOR environment variable is set to ${JAVA_VENDOR} in "<GigaSpaces Root>\bin\setenv.sh"
  # echo PRODUCTION_MODE environment variable is set to ${PRODUCTION_MODE} in "<GigaSpaces Root>\bin\setenv.sh"
fi
export JAVA_HOME JAVA_VENDOR PRODUCTION_MODE

# Set up JVM options base on value of JAVA_VENDOR
if [ "$PRODUCTION_MODE" = "true" ]; then
  case $JAVA_VENDOR in
  BEA)
    JAVA_OPTIONS="${JAVA_OPTIONS} -server -showversion -Xmx512m"
#-Xgc:gencon"
  ;;
  HP)
    JAVA_OPTIONS="${JAVA_OPTIONS} -server -showversion -Xmx512m"
  ;;
  IBM)
    JAVA_OPTIONS="${JAVA_OPTIONS} -showversion -Xmx512m"
  ;;
  Sun)
    JAVA_OPTIONS="${JAVA_OPTIONS} -server -showversion -Xmx512m"
  ;;
  *)
    JAVA_OPTIONS="${JAVA_OPTIONS} -showversion -Xmx512m"
  ;;
  esac
else
  case $JAVA_VENDOR in
  BEA)
    JAVA_OPTIONS="${JAVA_OPTIONS} -server -showversion -Xmx256m -Xgcreport -Xmanagement -verbose:memory,cpuinfo"
  ;;
  HP)
    JAVA_OPTIONS="${JAVA_OPTIONS} -server -showversion -Xmx256m"
  ;;
  IBM)
    JAVA_OPTIONS="${JAVA_OPTIONS} -showversion -Xmx256m -verbose:gc -Xquickstart"
  ;;
  Sun)
    JAVA_OPTIONS="${JAVA_OPTIONS} -server -showversion -Xmx256m"
  ;;
  *)
    JAVA_OPTIONS="${JAVA_OPTIONS} -showversion -Xmx256m"
  ;;
  esac
fi
export JAVA_OPTIONS


if [ "${JSHOMEDIR}" = "" ] ; then 
  JSHOMEDIR=`dirname $0`/..
fi
export JSHOMEDIR

## Append all files of lib/ext directory to the classpath
for i in ${JSHOMEDIR}/lib/ant/*.jar
do
    ANT_JARS=${ANT_JARS}$CPS$i
done
export ANT_JARS

for i in ${JSHOMEDIR}/lib/hibernate/*.jar
do
    HIBERNATE_JARS=${HIBERNATE_JARS}$CPS$i
done
export HIBERNATE_JARS

for i in ${JSHOMEDIR}/lib/jdbc/*.*
do
    JDBC_JARS=${JDBC_JARS}$CPS$i
done
export JDBC_JARS

for i in ${JSHOMEDIR}/lib/xml/*.jar
do
    XML_JARS=${XML_JARS}$CPS$i
done
export XML_JARS

for i in ${JSHOMEDIR}/lib/jmx/*.jar
do
    JMX_JARS=${JMX_JARS}$CPS$i
done
export JMX_JARS

for i in ${JSHOMEDIR}/lib/ui/*.jar
do
    UI_JARS=${UI_JARS}$CPS$i
done
export UI_JARS

for i in ${JSHOMEDIR}/lib/common/*.jar
do
    COMMON_JARS=${COMMON_JARS}$CPS$i
done
export COMMON_JARS

for i in ${JSHOMEDIR}/lib/spring/*.jar
do
    SPRING_JARS=${SPRING_JARS}$CPS$i
done
export SPRING_JARS

for i in ${JSHOMEDIR}/lib/ext/*.*
do
    EXT_JARS=${EXT_JARS}$CPS$i
done
export EXT_JARS

# The GS_JARS contains the same list as defined in the Class-Path entry of the JSpaces.jar manifest file.
# These jars are required for client application and starting a Space from within your application.
# Note - Do not set the GS_JARS variable together with the GS_JINI_START_CLASSPATH variable (which is used only for the ServiceStarter).
GS_JARS=${EXT_JARS}$CPS${JSHOMEDIR}$CPS${JSHOMEDIR}/lib/JSpaces.jar$CPS${JSHOMEDIR}/lib/jini/jsk-platform.jar$CPS${JSHOMEDIR}/lib/jini/jsk-lib.jar$CPS${JSHOMEDIR}/lib/jini/start.jar$CPS${JSHOMEDIR}/lib/ServiceGrid/gs-lib.jar$CPS${JSHOMEDIR}/lib/common/backport-util-concurrent.jar

PLATFORM_VERSION=6.0; export PLATFORM_VERSION
POLICY=${JSHOMEDIR}/policy/policy.all; export POLICY

if [ "${LOOKUPGROUPS}" = "" ] ; then
LOOKUPGROUPS="gigaspaces-6.0EE"; export LOOKUPGROUPS
fi
LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=${LOOKUPGROUPS}; export LOOKUP_GROUPS_PROP

if [ "${LOOKUPLOCATORS}" = "" ] ; then
LOOKUPLOCATORS=""; export LOOKUPLOCATORS
fi
LOOKUP_LOCATORS_PROP="-Dcom.gs.jini_lus.locators=${LOOKUPLOCATORS}"; export LOOKUP_LOCATORS_PROP

# The NIC_ADDR represents the specific network interface card IP address or the default host name.
# Note - When using Multi Network-Interface cards, explicitly set it with the NIC address (e.g. 192.168.0.2)
# as it is the value of the java.rmi.server.hostname system property. (see RMI_OPTIONS)
if [ "${NIC_ADDR}" = "" ] ; then
NIC_ADDR=`hostname`; export NIC_ADDR
fi

# Note: In a setup for Multi Network-Interface cards please append -Djava.rmi.server.hostname=${NIC_ADDR} 
# to the RMI_OPTIONS with proper network-interface IP address
if [ "${RMI_OPTIONS}" = "" ] ; then 
  RMI_OPTIONS="-Dsun.rmi.dgc.client.gcInterval=600000 -Dsun.rmi.dgc.server.gcInterval=600000 -Djava.rmi.server.RMIClassLoaderSpi=default -Djava.rmi.server.logCalls=false"
fi
export RMI_OPTIONS

# Note - Do not set the GS_JARS variable together with the GS_JINI_START_CLASSPATH variable (which is used only for the ServiceStarter).
GS_JINI_START_CLASSPATH="${EXT_JARS}$CPS${JSHOMEDIR}$CPS${JSHOMEDIR}/lib/jini/start.jar$CPS${JSHOMEDIR}/lib/ServiceGrid/gs-lib.jar"; export GS_JINI_START_CLASSPATH

# For remote Eclipse debugging add the ${ECLIPSE_REMOTE_DEBUG} variable to the command line:
ECLIPSE_REMOTE_DEBUG="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"; export ECLIPSE_REMOTE_DEBUG

# Set and add the system property -Djava.util.logging.config.file to the command line. It indicates file path to 
# the Java logging file location. Use it to enable finest logging troubleshooting of various Jini Services and GS modules.
# Setting this property will redirect all Jini services and GS modules output messages to a file.
GS_LOGGING_CONFIG_FILE_PROP="-Djava.util.logging.config.file=${JSHOMEDIR}/config/gs_logging.properties"; export GS_LOGGING_CONFIG_FILE_PROP

# Enable monitoring and management from remote systems using JMX jconsole.
REMOTE_JMX="-Dcom.sun.management.jmxremote.port=5001 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"; export REMOTE_JMX

if $VERBOSE; then
	echo ====================================================================================
	echo VERBOSE is on
	echo
	echo GigaSpaces ${PLATFORM_VERSION} environment set successfully from JSHOMEDIR: ${JSHOMEDIR}
	echo
	echo JAVACMD: ${JAVACMD}		JAVACCMD: ${JAVACCMD}		JAVAWCMD: ${JAVAWCMD}
	echo
	echo JAVA_OPTIONS: ${JAVA_OPTIONS}
	echo
	echo NIC_ADDR: ${NIC_ADDR}
	echo
	echo RMI_OPTIONS: ${RMI_OPTIONS}
	echo
	echo GS_JARS: ${GS_JARS}
	echo
	echo GS_JINI_START_CLASSPATH: ${GS_JINI_START_CLASSPATH}
	echo
	echo LOOKUPGROUPS: ${LOOKUPGROUPS}  LOOKUP_LOCATORS_PROP: ${LOOKUP_LOCATORS_PROP}
	echo
	echo ANT_JARS: ${ANT_JARS}
	echo
	echo HIBERNATE_JARS: ${HIBERNATE_JARS}
	echo
	echo JDBC_JARS: ${JDBC_JARS}
	echo
	echo XML_JARS: ${XML_JARS}
	echo
	echo UI_JARS: ${UI_JARS}
	echo
	echo JMX_JARS: ${JMX_JARS}
	echo
	echo COMMON_JARS: ${COMMON_JARS}
	echo
	echo SPRING_JARS: ${SPRING_JARS}
	echo
	echo EXT_JARS: ${EXT_JARS}
	echo
	echo GS_LOGGING_CONFIG_FILE_PROP: ${GS_LOGGING_CONFIG_FILE_PROP}
	echo
	echo ====================================================================================
else
  echo Environment set successfully from ${JSHOMEDIR}
fi
