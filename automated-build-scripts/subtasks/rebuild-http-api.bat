call %~dp0../_constants.bat

echo Begin Building HTTP API

cd %~dp0../../src/http-api

call mvn clean package

set deployCmd=mvn deploy:deploy-file -Durl=file:../../local-repository -Dfile=target/http-%httpApiVersion%.jar -DgroupId=com.cyecize  -DartifactId=http -Dpackaging=jar -Dversion=%httpApiVersion%
echo %deployCmd%

call %deployCmd%

cd %~dp0

echo Deployment of HTTP API completed successfuly