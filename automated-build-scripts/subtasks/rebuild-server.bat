cd %~dp0../../src/javache-web-server/server
call mvn clean package
call mvn deploy:deploy-file -Durl=file:../../../local-repository -Dfile=target/javache-1.3.jar -DgroupId=com.cyecize  -DartifactId=javache -Dpackaging=jar -Dversion=1.3 -DpomFile=javache.pom.txt