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
@rem JSHOMEDIR - The GigaSpaces home directory.
@rem            
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
@rem  For additional information, refer to the GigaSpaces OnLine Documentation
@rem  at http://www.gigaspaces.com/wiki/display/XAP71/System+Environment+and+Environment+Variables
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

rem The following parameters have been found optimal when using Solaris 10 on Sun CoolThreads Servers T1000/T2000 (Niagara) 
rem during extensive tests, using Sun JVM 1.5.0_06 and JVM 6.
rem In order to apply these VM switches, please add them to the JAVA_OPTIONS variable:

rem Optimal performance has been achieved with 2GB heap, it should be adjusted to real RAM size
rem -Xms2g �Xmx2g
rem -XX:+UseParallelOldGC
rem GC Threads quantity is, by default, equal to quantity of CPUs; 
rem On single/dual CPU systems recommended to be set as 4 - 8
rem -XX:ParallelGCThreads=32

rem Bundle of JVM options planned as default for upcoming release. 
rem Provides boost about 7-8%.
rem -XX:+AggressiveOpts
rem -XX:NewRatio=2                  
rem -XX:SurvivorRatio=32 
rem -XX:MaxTenuringThreshold=4
rem Relevant to Solaris 10
rem -XX:LargePageSizeInBytes=256m

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

set LCP=.
for %%i in ("%JSHOMEDIR%\lib\openspaces\*.*") do call %JSHOMEDIR%\bin\lcp %%i
set OPENSPACES_JARS=%LCP%

rem the GS_JARS contains the same list as defined in the Class-Path entry of the JSpaces.jar manifest file.
rem These jars are required for client application and starting a Space from within your application.
set GS_JARS=%EXT_JARS%;%JSHOMEDIR%;%JSHOMEDIR%/lib/JSpaces.jar;%JSHOMEDIR%/lib/jini/jsk-platform.jar;%JSHOMEDIR%/lib/jini/jsk-lib.jar;%JSHOMEDIR%/lib/jini/start.jar;%JSHOMEDIR%/lib/common/backport-util-concurrent.jar;%JSHOMEDIR%/lib/ServiceGrid/gs-lib.jar;%JSHOMEDIR%/lib/ServiceGrid/gs-boot.jar

set PLATFORM_VERSION=6.1
set POLICY=%JSHOMEDIR%\policy\policy.all

if "%LOOKUPGROUPS%" == ""  (
set LOOKUPGROUPS="gigaspaces-6.1XAP"
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

rem For remote Eclipse debugging add the "%ECLIPSE_REMOTE_DEBUG%" variable to the command line:
set ECLIPSE_REMOTE_DEBUG=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y

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
