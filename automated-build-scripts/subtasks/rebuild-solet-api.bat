cd %~dp0../../src/solet-api
call mvn clean package
call mvn deploy:deploy-file -Durl=file:../../local-repository -Dfile=target/solet-1.3.jar -DgroupId=com.cyecize  -DartifactId=solet -Dpackaging=jar -Dversion=1.3 -DpomFile=solet-deploy.pom.txt