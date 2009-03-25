@if "%DEBUG%" == "" @echo off

@rem 
@rem $Revision: 9540 $ $Date: 2007-11-29 18:00:39 +0100 (Do, 29. Nov 2007) $
@rem

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

:begin
@rem Determine what directory it is in.
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

set CLASSNAME=groovy.ui.InteractiveShell
if "%OLDSHELL%" == "" set CLASSNAME=org.codehaus.groovy.tools.shell.Main

"%DIRNAME%\startGroovy.bat" "%DIRNAME%" %CLASSNAME% %*

@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal
