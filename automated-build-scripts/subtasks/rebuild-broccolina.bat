cd %~dp0../../src/broccolina-request-handler
call mvn clean package
call mvn deploy:deploy-file -Durl=file:../../local-repository -Dfile=target/broccolina-1.3.jar -DgroupId=com.cyecize  -DartifactId=broccolina -Dpackaging=jar -Dversion=1.3 -DpomFile=broccolina.pom.txt