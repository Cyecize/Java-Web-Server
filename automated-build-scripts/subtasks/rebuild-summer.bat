call %~dp0../_constants.bat

echo Begin building SUMMER MVC

cd %~dp0../../src/summer-mvc-framework

call mvn clean
call mvn install dependency:copy-dependencies
call mvc package


cd %~dp0

echo Completed build of SUMMER MVC