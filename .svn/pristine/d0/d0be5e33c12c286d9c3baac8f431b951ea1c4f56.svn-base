@echo off

cd %~dp0

set CONSOLE_PATH=%cd%

setlocal ENABLEDELAYEDEXPANSION

for /r %%t in (*.jar) do set PRE_CLASSPATH=%%t;!PRE_CLASSPATH!

java  -Xms512m -Xmx1024m -cp ".;%PRE_CLASSPATH%" -Dfile.encoding=UTF-8  com.istar.mediabroken.console.ConsoleManager %1 %CONSOLE_PATH%

pause