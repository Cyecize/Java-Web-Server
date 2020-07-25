cd %~dp0../../src/coyote-resource-handler
call mvn clean package
call mvn deploy:deploy-file -Durl=file:../../local-repository -Dfile=target/original-toyote-1.3.jar -DgroupId=com.cyecize  -DartifactId=toyote -Dpackaging=jar -Dversion=1.3 -DpomFile=toyote.pom.txt