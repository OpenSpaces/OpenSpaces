@echo off

@call %~dp0\..\..\..\bin\setenv.bat

@%JAVACMD% %LOOKUP_GROUPS_PROP% -classpath %ANT_JARS%;"%JAVA_HOME%/lib/tools.jar" org.apache.tools.ant.Main %1
