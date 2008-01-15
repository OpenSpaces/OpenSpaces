@echo off

@rem The call to setenv.bat can be commented out if necessary.
@call %~dp0\setenv.bat

@echo Starting a Processing Unit Instance
@set JSHOMEDIR=%~dp0\..

FOR /F "tokens=*" %i IN ('%JAVACMD% -cp %JSHOMEDIR%/lib/JSpaces.jar;%JSHOMEDIR%/lib/openspaces/openspaces.jar org.openspaces.maven.support.OutputVersion') DO set VERSION=%i

%JAVACMD% -cp %JSHOMEDIR%/lib/JSpaces.jar;%JSHOMEDIR%/lib/openspaces/openspaces.jar org.openspaces.maven.support.POMGenerator %TEMP%

echo ""
echo ""
echo "Installing Version %VERSION%"
echo ""
echo ""

# Jini Jars
mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-start -Dversion=%VERSION% -DpomFile=%TEMP%/jini-start-pom.xml -Dpackaging=jar -Dfile=%JSHOMEDIR%/lib/jini/start.jar
mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-jsk-lib -Dversion=%VERSION% -DpomFile=%TEMP%/jini-jsk-lib-pom.xml -Dpackaging=jar -Dfile=%JSHOMEDIR%/lib/jini/jsk-lib.jar
mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-jsk-platform -Dversion=%VERSION% -DpomFile=%TEMP%/jini-jsk-platform-pom.xml -Dpackaging=jar -Dfile=%JSHOMEDIR%/lib/jini/jsk-platform.jar
mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-jsk-resources -Dversion=%VERSION% -DpomFile=%TEMP%/jini-jsk-resources-pom.xml -Dpackaging=jar -Dfile=%JSHOMEDIR%/lib/jini/jsk-resources.jar
mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-reggie -Dversion=%VERSION% -DpomFile=%TEMP%/jini-reggie-pom.xml -Dpackaging=jar -Dfile=%JSHOMEDIR%/lib/jini/reggie.jar
mvn install:install-file -DgroupId=gigaspaces -DartifactId=jini-mahalo -Dversion=%VERSION% -DpomFile=%TEMP%/jini-mahalo-pom.xml -Dpackaging=jar -Dfile=%JSHOMEDIR%/lib/jini/mahalo.jar

# GigaSpaces Jars
mvn install:install-file -DgroupId=gigaspaces -DartifactId=gs-boot -Dversion=%VERSION% -DpomFile=%TEMP%/gs-boot-pom.xml -Dpackaging=jar -Dfile=%JSHOMEDIR%/lib/ServiceGrid/gs-boot.jar
mvn install:install-file -DgroupId=gigaspaces -DartifactId=JSpaces -Dversion=%VERSION% -DpomFile=%TEMP%/JSpaces-pom.xml -Dpackaging=jar -Dfile=%JSHOMEDIR%/lib/JSpaces.jar
mvn install:install-file -DgroupId=gigaspaces -DartifactId=openspaces -Dversion=%VERSION% -DpomFile=%TEMP%/openspaces-pom.xml -Dpackaging=jar -Dfile=%JSHOMEDIR%/lib/openspaces/openspaces.jar
mvn install:install-file -DgroupId=gigaspaces -DartifactId=mule-os -Dversion=%VERSION% -Dpackaging=jar -DpomFile=%TEMP%/mule-os-pom.xml -Dfile=%JSHOMEDIR%/lib/openspaces/mule-os.jar

# Copy licenese file
copy %JSHOMEDIR%\gslicense.xml %HOMEDRIVE%\%HOMEPATH%\.m2\repository\gigaspaces\gs-boot\%VERSION%

