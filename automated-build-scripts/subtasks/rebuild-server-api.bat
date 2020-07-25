cd %~dp0../../src/javache-web-server/api
call mvn clean package

call mvn deploy:deploy-file -Durl=file:../../../local-repository -Dfile=target/javache-api-1.3.jar -DgroupId=com.cyecize  -DartifactId=javache-api -Dpackaging=jar -Dversion=1.3 -DpomFile=javache-api-1.3.pom.txt