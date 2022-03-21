call %~dp0../_constants.bat

cd %~dp0../../src/solet-api

echo Start Building SOLET API

call mvn clean package

set deployCommand=mvn deploy:deploy-file -Durl=file:../../local-repository -Dfile=target/solet-%soletApiVersion%.jar -DgroupId=com.cyecize  -DartifactId=solet -Dpackaging=jar -Dversion=%soletApiVersion% -DpomFile=solet-deploy.pom.txt
echo %deployCommand%

call %deployCommand%

cd %~dp0

echo Complete building SOLET API