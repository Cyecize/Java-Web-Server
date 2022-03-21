call %~dp0../_constants.bat

cd %~dp0../../src/broccolina-request-handler
call mvn clean package
call mvn deploy:deploy-file -Durl=file:../../local-repository -Dfile=target/broccolina-%broccolinaVersion%.jar -DgroupId=com.cyecize  -DartifactId=broccolina -Dpackaging=jar -Dversion=%broccolinaVersion% -DpomFile=broccolina.pom.txt

cd %~dp0