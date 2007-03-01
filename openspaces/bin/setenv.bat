@rem *************************************************************************
@rem This script is used to initialize common environment to GigaSpaces Server.
@rem
@rem It sets the following variables:
@rem
@rem JAVA_HOME  - Location of the JDK version used to start GigaSpaces 
@rem              Server.
@rem          Note that YOU MUST SUPPLY A JAVA_HOME environment variable.
@rem JAVACMD - The Java command-line
@rem  
@rem JAVA_OPTIONS   - Java command-line options for running the server,
@rem            Including: The Java args to override the standard memory arguments passed to java,
@rem            - Arg specifying the JVM to run.  (i.e. -server, -hotspot, -jrocket etc.)
@rem            - GC, profiling and management options.
@rem JAVA_VENDOR
@rem            - Vendor of the JVM (i.e. All, BEA, HP, IBM, Sun, etc.) 
@rem		- Default is ALL, meaning general settings
@rem RMI_OPTIONS
@rem            - Additional RMI optional properties.  
@rem JAVA_VM - The java arg specifying the JVM to run.  (i.e. 
@rem              -server, -hotspot, -jrocket etc.)
@rem
@rem JSHOMEDIR
@rem            - The GigaSpaces home directory.
@rem POLICY - The default security policy file.
@rem PRODUCTION_MODE
@rem            - Indicates if GigaSpaces Server will be started in Production
@rem              mode (default to the production mode).
@rem LOOKUPGROUPS - Jini Lookup Service Group
@rem
@rem LOOKUPLOCATORS - Jini Lookup Service Locators used for unicast discovery
@rem
@rem NIC_ADDR 	  - The Network Interface card IP Address
@rem 
@rem GS_JINI_START_CLASSPATH - The jars needed for starting the gsServer instance.
@rem
@rem  For additional information, refer to the GigaSpaces OnLine Documentation
@rem  at http://www.gigaspaces.com/docs.htm
@rem *************************************************************************
@echo off

@rem - Set VERBOSE=true for debugging output
if "%VERBOSE%" == "" (
	@set VERBOSE=false
)

@rem - Set or override the JAVA_HOME variable
@rem - By default, the system value is used.
@rem set JAVA_HOME="D:\jdk1.5.0_04"
@rem - Reset JAVA_HOME unless JAVA_HOME is pre-defined.
if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome

@echo JAVA_HOME environment variable is set to %JAVA_HOME% in "<GigaSpaces Root>\bin\setenv.bat"
set JAVACMD="%JAVA_HOME%\bin\java"
set JAVACCMD="%JAVA_HOME%\bin\javac"
set JAVAWCMD="%JAVA_HOME%\bin\javaw"
goto endOfJavaHome

:noJavaHome
@echo The JAVA_HOME environment variable is not set. Using the java that is set in system path."
set JAVACMD=java
set JAVACCMD=javac
set JAVAWCMD=javaw

:endOfJavaHome


@rem Reset JAVA_VENDOR and PRODUCTION_MODE unless JAVA_VENDOR is defined already.
if DEFINED JAVA_VENDOR goto noReset
@rem JAVA VENDOR, possible values are:
@rem ALL, BEA, HP, IBM, Sun, etc. - Default is ALL, meaning general settings
set  JAVA_VENDOR=ALL

if "%PRODUCTION_MODE%" == "" (
	@rem PRODUCTION_MODE, default to the production mode
	set PRODUCTION_MODE=true
)
@rem echo JAVA_VENDOR environment variable is set to %JAVA_VENDOR% in "<GigaSpaces Root>\bin\setenv.bat"
@rem echo PRODUCTION_MODE environment variable is set to %PRODUCTION_MODE% in "<GigaSpaces Root>\bin\setenv.bat"

:noReset
@rem set up JVM Vendors
if "%JAVA_VENDOR%" == "ALL" goto all
if "%JAVA_VENDOR%" == "BEA" goto bea
if "%JAVA_VENDOR%" == "Sun" goto sun
if "%JAVA_VENDOR%" == "IBM" goto ibm
if "%JAVA_VENDOR%" == "HP"  goto hp
goto continue

:all
if "%PRODUCTION_MODE%" == "true" goto all_prod_mode
set JAVA_OPTIONS= -showversion -Xmx256m
goto continue
:all_prod_mode
set JAVA_OPTIONS= -server -showversion -Xmx512m
goto continue

:bea
if "%PRODUCTION_MODE%" == "true" goto bea_prod_mode
set JAVA_OPTIONS= -server -showversion -Xmx256m -Xgcreport -Xmanagement -verbose:memory,cpuinfo
goto continue
:bea_prod_mode
@rem more available options:  -Xgcprio:pausetime -XpauseTarget=200ms
set JAVA_OPTIONS= -server -showversion -Xmx512m -Xgcprio:throughput
goto continue

:sun
if "%PRODUCTION_MODE%" == "true" goto sun_prod_mode
set JAVA_OPTIONS= -server -showversion -Xmx256m
goto continue
:sun_prod_mode
set JAVA_OPTIONS= -server -showversion -Xmx512m
goto continue

:ibm
if "%PRODUCTION_MODE%" == "true" goto ibm_prod_mode
@rem More optional options: -Xrunhprof
set JAVA_OPTIONS= -showversion -Xmx256m -verbose:gc -Xquickstart
goto continue
:ibm_prod_mode
set JAVA_OPTIONS= -showversion -Xmx512m
goto continue

:hp
if "%PRODUCTION_MODE%" == "true" goto hp_prod_mode
set JAVA_OPTIONS= -server -showversion -Xmx256m
goto continue
:hp_prod_mode
set JAVA_OPTIONS= -server -showversion -Xmx512m
goto continue


:continue
if "%JSHOMEDIR%" == "" set JSHOMEDIR=%~dp0\..

rem Append all files of lib/ext directory to the classpath
set LCP=%JSHOMEDIR%\lib

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\ant\*.jar") do call %JSHOMEDIR%\bin\lcp %%i
set ANT_JARS=%LCP%

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\hibernate\*.jar") do call %JSHOMEDIR%\bin\lcp %%i
set HIBERNATE_JARS=%LCP%

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\jdbc\*.jar") do call %JSHOMEDIR%\bin\lcp %%i
for %%i in ("%JSHOMEDIR%\lib\jdbc\*.zip") do call %JSHOMEDIR%\bin\lcp %%i
set JDBC_JARS=%LCP%

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\xml\*.jar") do call %JSHOMEDIR%\bin\lcp %%i
set XML_JARS=%LCP%

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\ui\*.jar") do call %JSHOMEDIR%\bin\lcp %%i
set UI_JARS=%LCP%

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\jmx\*.jar") do call %JSHOMEDIR%\bin\lcp %%i
set JMX_JARS=%LCP%

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\common\*.jar") do call %JSHOMEDIR%\bin\lcp %%i
set COMMON_JARS=%LCP%

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\spring\*.jar") do call %JSHOMEDIR%\bin\lcp %%i
set SPRING_JARS=%LCP%

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\ext\*.*") do call %JSHOMEDIR%\bin\lcp %%i
set EXT_JARS=%LCP%

rem the GS_JARS contains the same list as defined in the Class-Path entry of the JSpaces.jar manifest file.
rem These jars are required for client application and starting a Space from within your application.
rem Note - Do not set the GS_JARS variable together with the GS_JINI_START_CLASSPATH variable (which is used only for the ServiceStarter).
set GS_JARS=%EXT_JARS%;%JSHOMEDIR%;%JSHOMEDIR%/lib/JSpaces.jar;%JSHOMEDIR%/lib/jini/jsk-platform.jar;%JSHOMEDIR%/lib/jini/jsk-lib.jar;%JSHOMEDIR%/lib/jini/start.jar;%JSHOMEDIR%/lib/common/backport-util-concurrent.jar;%JSHOMEDIR%/lib/ServiceGrid/gs-lib.jar

set PLATFORM_VERSION=6.0
set POLICY=%JSHOMEDIR%\policy\policy.all

if "%LOOKUPGROUPS%" == ""  (
set LOOKUPGROUPS="gigaspaces-%USERNAME%"
)
set LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=%LOOKUPGROUPS%

if "%LOOKUPLOCATORS%" == ""  (
set LOOKUPLOCATORS=""
)
set LOOKUP_LOCATORS_PROP=-Dcom.gs.jini_lus.locators=%LOOKUPLOCATORS%

rem The NIC_ADDR represents the specific network interface card IP address or the default host name.
rem Note - When using Multi Network-Interface cards, explicitly set it with the NIC address (e.g. 192.168.0.2)
rem as it is the value of the the java.rmi.server.hostname system property. (see RMI_OPTIONS)
if "%NIC_ADDR%" == "" (
set NIC_ADDR="%COMPUTERNAME%"
)

rem RMI properties
rem Note: In a setup for Multi Network-Interface cards please append -Djava.rmi.server.hostname="%NIC_ADDR%" 
rem with proper network-interface IP address to the RMI_OPTIONS
set RMI_OPTIONS=-Dsun.rmi.dgc.client.gcInterval=600000 -Dsun.rmi.dgc.server.gcInterval=600000 -Djava.rmi.server.RMIClassLoaderSpi=default -Djava.rmi.server.logCalls=false

rem Note - Do not set the GS_JARS variable together with the GS_JINI_START_CLASSPATH variable (which is used only for the ServiceStarter).
set GS_JINI_START_CLASSPATH=%EXT_JARS%;%JSHOMEDIR%;%JSHOMEDIR%/lib/jini/start.jar;%JSHOMEDIR%/lib/ServiceGrid/gs-lib.jar

rem For remote Eclipse debugging add the "%ECLIPSE_REMOTE_DEBUG%" variable to the command line:
set ECLIPSE_REMOTE_DEBUG=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000

rem Set and add the system property -Djava.util.logging.config.file to the command line. It indicates file path to 
rem the Java logging file location. Use it to enable finest logging troubleshooting of various Jini Services and GS modules.
rem Setting this property will redirect all Jini services and GS modules output messages to a file.
rem Specific logging settings can be provided by setting the following before calling setenv:
rem export GS_LOGGING_CONFIG_FILE=/somepath/my_logging.properties
if "%GS_LOGGING_CONFIG_FILE%" == "" (
set GS_LOGGING_CONFIG_FILE="%JSHOMEDIR%/config/gs_logging.properties"
)
set GS_LOGGING_CONFIG_FILE_PROP=-Djava.util.logging.config.file=%GS_LOGGING_CONFIG_FILE%

rem Enable monitoring and management from remote systems using JMX jconsole.
set REMOTE_JMX=-Dcom.sun.management.jmxremote.port=5001 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false

if "%VERBOSE%"=="true" (
	echo ====================================================================================
	echo VERBOSE is on
	echo.
	echo GigaSpaces %PLATFORM_VERSION% environment set successfully from JSHOMEDIR: %JSHOMEDIR%
	echo.
	echo JAVACMD: %JAVACMD%    		JAVACCMD: %JAVACCMD%		JAVAWCMD: %JAVAWCMD%
	echo.
	echo JAVA_OPTIONS: %JAVA_OPTIONS%
	echo.
	echo NIC_ADDR: %NIC_ADDR%
	echo.
	echo RMI_OPTIONS: %RMI_OPTIONS%
	echo.
	echo GS_JARS: %GS_JARS%
	echo.
	echo GS_JINI_START_CLASSPATH: %GS_JINI_START_CLASSPATH%
	echo.
	echo LOOKUPGROUPS: %LOOKUPGROUPS%  LOOKUPLOCATORS: %LOOKUPLOCATORS%
	echo.
	echo ANT_JARS: %ANT_JARS%
	echo.
	echo HIBERNATE_JARS: %HIBERNATE_JARS%
	echo.
	echo JDBC_JARS: %JDBC_JARS%
	echo.
	echo XML_JARS: %XML_JARS%
	echo.
	echo UI_JARS: %UI_JARS%
	echo.
	echo JMX_JARS: %JMX_JARS%
	echo.
	echo COMMON_JARS: %COMMON_JARS%
	echo.
	echo SPRING_JARS: %SPRING_JARS%
	echo.
	echo EXT_JARS: %EXT_JARS%
	echo.
	echo GS_LOGGING_CONFIG_FILE_PROP: %GS_LOGGING_CONFIG_FILE_PROP%
	echo.
	echo ====================================================================================
) else (
	echo Environment set successfully from %JSHOMEDIR%
)

:end
