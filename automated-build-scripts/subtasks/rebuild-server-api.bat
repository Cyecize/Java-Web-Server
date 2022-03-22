call %~dp0../_constants.bat

cd %~dp0../../src/javache-web-server/api
call mvn clean package dependency:copy-dependencies

call mvn deploy:deploy-file -Durl=file:../../../local-repository -Dfile=target/javache-api-%javacheApiVersion%.jar -DgroupId=com.cyecize  -DartifactId=javache-api -Dpackaging=jar -Dversion=%javacheApiVersion% -DpomFile=javache-api-%javacheApiVersion%.pom.txt

cd %~dp0