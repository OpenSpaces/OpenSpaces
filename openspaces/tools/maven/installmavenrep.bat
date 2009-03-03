@echo off

if "%1" == ""  (
@rem The call to setenv.bat can be commented out if necessary.
@call "%~dp0\..\..\bin\setenv.bat"
)

if "%1" == "version"  (
%JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.OutputVersion
goto END
)

FOR /F "usebackq tokens=*" %%i IN (`installmavenrep.bat version`) DO @set VERSION=%%i

%JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.POMGenerator "%TEMP%" %VERSION% "%JSHOMEDIR%/tools/maven/maven-openspaces-plugin"

echo ""
echo ""
echo "Installing Version %VERSION%"
echo ""
echo ""

REM Jini Jars
call mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-start -Dversion=%VERSION% -DpomFile=%TEMP%/jini-start-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/start.jar"
call mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-jsk-lib -Dversion=%VERSION% -DpomFile=%TEMP%/jini-jsk-lib-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/jsk-lib.jar"
call mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-jsk-platform -Dversion=%VERSION% -DpomFile=%TEMP%/jini-jsk-platform-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/jsk-platform.jar"
call mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-jsk-resources -Dversion=%VERSION% -DpomFile=%TEMP%/jini-jsk-resources-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/jsk-resources.jar"
call mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-reggie -Dversion=%VERSION% -DpomFile=%TEMP%/jini-reggie-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/reggie.jar"
call mvn install:install-file -DgroupId=com.gigaspaces.jini -DartifactId=jini-mahalo -Dversion=%VERSION% -DpomFile=%TEMP%/jini-mahalo-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jini/mahalo.jar"

REM GigaSpaces Jars
call mvn install:install-file -DgroupId=com.gigaspaces.core -DartifactId=gs-boot -Dversion=%VERSION% -DpomFile=%TEMP%/gs-boot-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/ServiceGrid/gs-boot.jar"
call mvn install:install-file -DgroupId=com.gigaspaces.core -DartifactId=gs-service -Dversion=%VERSION% -DpomFile=%TEMP%/gs-service-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/ServiceGrid/gs-service.jar"
call mvn install:install-file -DgroupId=com.gigaspaces.core -DartifactId=gs-lib -Dversion=%VERSION% -DpomFile=%TEMP%/gs-lib-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/ServiceGrid/gs-lib.jar"
call mvn install:install-file -DgroupId=com.gigaspaces.core -DartifactId=JSpaces -Dversion=%VERSION% -DpomFile=%TEMP%/JSpaces-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/JSpaces.jar"
call mvn install:install-file -DgroupId=org.openspaces -DartifactId=openspaces -Dversion=%VERSION% -DpomFile=%TEMP%/openspaces-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/openspaces/openspaces.jar"
call mvn install:install-file -DgroupId=org.openspaces -DartifactId=mule-os -Dversion=%VERSION% -Dpackaging=jar -DpomFile=%TEMP%/mule-os-pom.xml -Dfile="%JSHOMEDIR%/lib/openspaces/mule-os.jar"

REM JMX Jars
call mvn install:install-file -DgroupId=com.sun.jdmk -DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jmx/jmxtools.jar"
call mvn install:install-file -DgroupId=javax.management -DartifactId=jmxremote -Dversion=1.0.1_04 -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jmx/jmxremote.jar"
call mvn install:install-file -DgroupId=javax.management -DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/jmx/jmxri.jar"

REM Build and install OpenSpaces Maven Plugin 
call mvn -f maven-openspaces-plugin/pom.xml install 

REM Copy licenese file
call mvn os:install-license -Dfile="%JSHOMEDIR%\gslicense.xml" -Dversion=%VERSION%

REM remove temp files
del %TEMP%\jini-start-pom.xml
del %TEMP%\jini-jsk-lib-pom.xml
del %TEMP%\jini-jsk-platform-pom.xml
del %TEMP%\jini-jsk-resources-pom.xml
del %TEMP%\jini-reggie-pom.xml
del %TEMP%\jini-mahalo-pom.xml
del %TEMP%\gs-boot-pom.xml
del %TEMP%\gs-service-pom.xml
del %TEMP%\gs-lib-pom.xml
del %TEMP%\JSpaces-pom.xml
del %TEMP%\openspaces-pom.xml
del %TEMP%\mule-os-pom.xml

:END
