call %~dp0rebuild-summer.bat

cd %~dp0..\..\build-output
SET src=%~dp0..\..\src
SET third-party-repo=%~dp0..\..\third-party-repository

rmdir \Q \S javache-standard
mkdir javache-standard

cd javache-standard

mkdir api
copy %src%\http-api\target\http-1.3.jar api\http-1.3.jar
copy %src%\solet-api\target\solet-1.3.jar api\solet-1.3.jar

mkdir bin
copy %src%\javache-web-server\api\target\javache-api-1.3.jar bin\javache-api-1.3.jar
copy %third-party-repo%\magic-injector-fat-jar-1.2.jar bin\magic-injector-1.2.jar

mkdir config
copy %src%\javache-web-server\server\conf\config.ini config\config.ini
copy %src%\javache-web-server\server\conf\request-handlers.ini config\request-handlers.ini

mkdir lib
copy %src%\broccolina-request-handler\target\broccolina-1.3.jar lib\broccolina-1.3.jar
copy %src%\coyote-resource-handler\target\toyote-1.3.jar lib\toyote-1.3.jar
copy %src%\summer-mvc-framework\target\summer-1.3.jar lib\summer-1.3.jar

mkdir server
xcopy %src%\javache-web-server\server\target\classes server /E/H

mkdir webapps

copy %src%\javache-web-server\server\conf\StartJavache.bat StartJavache.bat

cd %~dp0