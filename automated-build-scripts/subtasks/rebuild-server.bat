call %~dp0../_constants.bat

cd %~dp0../../src/javache-web-server/server
call mvn clean package
call mvn deploy:deploy-file -Durl=file:../../../local-repository -Dfile=target/javache-%javacheServerVersion%.jar -DgroupId=com.cyecize  -DartifactId=javache -Dpackaging=jar -Dversion=%javacheServerVersion% -DpomFile=javache.pom.txt

cd %~dp0