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

REM GigaSpaces Jars
call mvn install:install-file -DgroupId=com.gigaspaces -DartifactId=gs-runtime -Dversion=%VERSION% -DpomFile=%TEMP%/gs-runtime-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/required/gs-runtime.jar"
call mvn install:install-file -DgroupId=com.gigaspaces -DartifactId=gs-openspaces -Dversion=%VERSION% -DpomFile=%TEMP%/gs-openspaces-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/required/gs-openspaces.jar"
call mvn install:install-file -DgroupId=com.gigaspaces -DartifactId=mule-os -Dversion=%VERSION% -Dpackaging=jar -DpomFile=%TEMP%/mule-os-pom.xml -Dfile="%JSHOMEDIR%/lib/optional/openspaces/mule-os.jar"

REM Build and install OpenSpaces Maven Plugin 
call mvn -f maven-openspaces-plugin/pom.xml install 

REM Copy licenese file
call mvn os:install-license -Dfile="%JSHOMEDIR%\gslicense.xml" -Dversion=%VERSION%

REM remove temp files
del %TEMP%\gs-runtime-pom.xml
del %TEMP%\gs-openspaces-pom.xml
del %TEMP%\mule-os-pom.xml

:END
