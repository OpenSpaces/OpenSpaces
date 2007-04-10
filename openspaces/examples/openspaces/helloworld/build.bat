@echo off

@call %~dp0\..\..\..\bin\setenv.bat

@%JAVACMD% %LOOKUP_GROUPS_PROP% -classpath %ANT_JARS% org.apache.tools.ant.Main %1
