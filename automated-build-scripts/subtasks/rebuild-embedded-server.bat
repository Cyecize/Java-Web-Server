call %~dp0../_constants.bat

cd %~dp0../../src/javache-web-server/server-embedded
call mvn clean package

cd %~dp0