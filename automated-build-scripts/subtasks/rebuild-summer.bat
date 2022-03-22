call %~dp0../_constants.bat

echo Begin building SUMMER MVC

cd %~dp0../../src/summer-mvc-framework

call mvn clean package dependency:copy-dependencies

cd %~dp0

echo Completed build of SUMMER MVC