@echo off

if "%1" == ""  (
@rem The call to setenv.bat can be commented out if necessary.
@call "%~dp0\..\..\bin\setenv.bat"
)

if "%1" == "edition"  (
%JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.OutputVersion edition
goto END
)

if "%1" == "XAP"  (
%JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.OutputVersion XAP
goto END
)

if "%1" == "Cloudify"  (
%JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.OutputVersion Cloudify
goto END
)

FOR /F "usebackq tokens=*" %%i IN (`installmavenrep.bat edition`) DO @set EDITION=%%i


FOR /F "usebackq tokens=*" %%i IN (`installmavenrep.bat XAP`) DO @set XAP_VERSION=%%i


%JAVACMD% -cp %GS_JARS% org.openspaces.maven.support.POMGenerator "%TEMP%" "%JSHOMEDIR%/tools/maven/maven-openspaces-plugin"

echo ""
echo ""
echo "Installing XAP %XAP_VERSION% jars"
echo ""
echo ""

REM GigaSpaces Jars
call mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-runtime -Dversion=%XAP_VERSION% -DpomFile=%TEMP%/gs-runtime-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/required/gs-runtime.jar" -Djavadoc="%JSHOMEDIR%/docs/javadoc.zip" 
call mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=gs-openspaces -Dversion=%XAP_VERSION% -DpomFile=%TEMP%/gs-openspaces-pom.xml -Dpackaging=jar -Dfile="%JSHOMEDIR%/lib/required/gs-openspaces.jar" -Djavadoc="%JSHOMEDIR%/docs/javadoc.zip" -Dsources="%JSHOMEDIR%/lib/optional/openspaces/gs-openspaces-src.zip"  
call mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=mule-os -Dversion=%XAP_VERSION% -Dpackaging=jar -DpomFile=%TEMP%/mule-os-pom.xml -Dfile="%JSHOMEDIR%/lib/optional/openspaces/mule-os.jar"
call mvn install:install-file -DgroupId=com.gigaspaces -DcreateChecksum=true -DartifactId=jetty-os -Dversion=%XAP_VERSION% -Dpackaging=jar -DpomFile=%TEMP%/jetty-os-pom.xml -Dfile="%JSHOMEDIR%/lib/platform/openspaces/gs-openspaces-jetty.jar"

REM Build and install OpenSpaces Maven Plugin 
call mvn -f maven-openspaces-plugin/pom.xml install  -DcreateChecksum=true

if "%EDITION%" == "XAP" (
REM Copy licenese file
call mvn os:install-license -Dfile="%JSHOMEDIR%\gslicense.xml" -Dversion=%XAP_VERSION%
)

if "%EDITION%" == "Cloudify" FOR /F "usebackq tokens=*" %%i IN (`installmavenrep.bat Cloudify`) DO @set CLOUDIFY_VERSION=%%i

if "%EDITION%" == "Cloudify" (

echo ""
echo ""
echo "Installing Cloudify %CLOUDIFY_VERSION% jars"
echo ""
echo ""

call mvn install:install-file -DgroupId=org.cloudifysource -DcreateChecksum=true -DartifactId=dsl -Dversion=%CLOUDIFY_VERSION% -Dpackaging=jar -DpomFile=%TEMP%/dsl-pom.xml -Dfile="%JSHOMEDIR%/lib/required/dsl.jar"
call mvn install:install-file -DgroupId=org.cloudifysource -DcreateChecksum=true -DartifactId=usm -Dversion=%CLOUDIFY_VERSION% -Dpackaging=jar -DpomFile=%TEMP%/usm-pom.xml -Dfile="%JSHOMEDIR%/lib/platform/usm/usm.jar"
)


REM remove temp files
del %TEMP%\gs-runtime-pom.xml
del %TEMP%\gs-openspaces-pom.xml
del %TEMP%\mule-os-pom.xml
del %TEMP%\jetty-os-pom.xml

if "%EDITION%" == "Cloudify" (
del %TEMP%\usm-pom.xml
del %TEMP%\dsl-pom.xml
)

:END
