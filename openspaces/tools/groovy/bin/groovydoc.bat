@if "%DEBUG%" == "" @echo off

@rem 
@rem $Revision: 19763 $ $Date: 2010-04-05 14:33:24 +0200 (Mo, 05. Apr 2010) $
@rem 

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

:begin
@rem Determine what directory it is in.
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\

"%DIRNAME%\startGroovy.bat" "%DIRNAME%" org.codehaus.groovy.tools.groovydoc.Main %*

@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal
%COMSPEC% /C exit /B %ERRORLEVEL%
