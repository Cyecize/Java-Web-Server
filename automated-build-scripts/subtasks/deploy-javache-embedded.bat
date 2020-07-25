call %~dp0rebuild-embedded-server.bat

cd %~dp0..\..\build-output
SET src=%~dp0..\..\src

rmdir \Q \S javache-embedded
mkdir javache-embedded

cd javache-embedded

copy %src%\javache-web-server\server-embedded\target\javache-embedded-1.3.jar javache-embedded-1.3.jar
copy %src%\javache-web-server\server-embedded\target\conf\javache-embedded-readme.txt javache-embedded-readme.txt

cd %~dp0