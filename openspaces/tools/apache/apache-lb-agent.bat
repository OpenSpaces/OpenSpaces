@echo off

@rem The call to setenv.bat can be commented out if necessary.
@call "%~dp0\..\..\bin\setenv.bat"

rem set booclasspath
set bootclasspath=-Xbootclasspath/p:%XML_JARS%


set LOOKUP_GROUPS_PROP=-Dcom.gs.jini_lus.groups=%LOOKUPGROUPS%

if "%LOOKUPLOCATORS%" == ""  (
set LOOKUPLOCATORS=
)
set LOOKUP_LOCATORS_PROP=-Dcom.gs.jini_lus.locators=%LOOKUPLOCATORS%

set COMMAND_LINE=%JAVACMD% %JAVA_OPTIONS% %bootclasspath% %LOOKUP_LOCATORS_PROP% %LOOKUP_GROUPS_PROP% %RMI_OPTIONS% "-Dcom.gs.home=%JSHOMEDIR%" -Djava.security.policy="%POLICY%" -classpath %PRE_CLASSPATH%;%COMMON_JARS%;%SPRING_JARS%;%EXT_JARS%;%JDBC_JARS%;"%JSHOMEDIR%";"%JSHOMEDIR%\lib\JSpaces.jar";%OPENSPACES_JARS%;%POST_CLASSPATH% org.openspaces.pu.container.jee.lb.apache.ApacheLoadBalancerAgent %*

set TITLE="Apache Load Balancer Agent ["%*"] started on [%computername%]"
@title %TITLE%

echo.
echo.
echo Starting apache-lb-agent with line:
echo %COMMAND_LINE%
echo.

%COMMAND_LINE%
goto end

:end
endlocal
title Command Prompt
set TITLE=
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
exit /B 0
