call %~dp0../_constants.bat

cd %~dp0../../src/broccolina-request-handler
call mvn clean package dependency:copy-dependencies

set deployCmd=mvn deploy:deploy-file -Durl=file:../../local-repository -Dfile=target/broccolina-%broccolinaVersion%.jar -DgroupId=com.cyecize  -DartifactId=broccolina -Dpackaging=jar -Dversion=%broccolinaVersion% -DpomFile=broccolina.pom.txt
echo %deployCmd%

call %deployCmd%

cd %~dp0

echo Broccolina successfuly deployed