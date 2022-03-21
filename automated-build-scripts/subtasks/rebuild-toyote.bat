call %~dp0../_constants.bat

cd %~dp0../../src/coyote-resource-handler
call mvn clean package

call mvn deploy:deploy-file -Durl=file:../../local-repository -Dfile=target/original-toyote-%toyoteVersion%.jar -DgroupId=com.cyecize  -DartifactId=toyote -Dpackaging=jar -Dversion=%toyoteVersion% -DpomFile=toyote.pom.txt

cd %~dp0