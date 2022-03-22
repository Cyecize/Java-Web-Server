call %~dp0../_constants.bat
call %~dp0rebuild-summer.bat

cd %~dp0..\..\build-output
SET src=%~dp0..\..\src
SET third-party-repo=%~dp0..\..\third-party-repository

rmdir "javache-standard" /S /Q
mkdir javache-standard

cd javache-standard

SET currentDir=%CD%

mkdir api
copy %src%\http-api\target\http-%httpApiVersion%.jar api\http-%httpApiVersion%.jar
copy %src%\solet-api\target\solet-%soletApiVersion%.jar api\solet-%soletApiVersion%.jar

mkdir bin
copy %src%\javache-web-server\api\target\javache-api-%javacheApiVersion%.jar bin\javache-api-%javacheApiVersion%.jar
copy %third-party-repo%\magic-injector-fat-jar-%magicInjectorVersion%.jar bin\magic-injector-%magicInjectorVersion%.jar

mkdir config
copy %src%\javache-web-server\server\conf\config.ini config\config.ini
copy %src%\javache-web-server\server\conf\request-handlers.ini config\request-handlers.ini

mkdir lib
copy %src%\broccolina-request-handler\target\broccolina-%broccolinaVersion%.jar lib\broccolina-%broccolinaVersion%.jar
:: Move Broccolina's dependencies
xcopy %src%\broccolina-request-handler\target\dependency lib /E/H

copy %src%\coyote-resource-handler\target\original-toyote-%toyoteVersion%.jar lib\toyote-%toyoteVersion%.jar
:: Move Toyote's dependencies
xcopy %src%\coyote-resource-handler\target\dependency lib /E/H

copy %src%\summer-mvc-framework\target\original-summer-%summerVersion%.jar lib\summer-%summerVersion%.jar
:: Move Summer MVC's dependencies
xcopy %src%\summer-mvc-framework\target\dependency lib /E/H

mkdir server
xcopy %src%\javache-web-server\server\target\classes server /E/H

mkdir webapps

copy %src%\javache-web-server\server\conf\StartJavache.bat StartJavache.bat


:: Deploy Summer MVC Fat far too
copy %src%\summer-mvc-framework\target\summer-%summerVersion%.jar ..\summer-%summerVersion%.jar

cd %~dp0