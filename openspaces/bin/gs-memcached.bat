@echo off

rem This script provides the command and control utility for the 
rem GigaSpaces Technologies gs-memcached script.
rem The gs-memcached script starts a memcached agent.

set MEMCACHED_URL=%1
if "%MEMCACHED_URL%" == "" set MEMCACHED_URL=/./memcached 

call "%~dp0\puInstance.bat" -properties space embed://url=%MEMCACHED_URL% "%~dp0\..\deploy\templates\memcached"