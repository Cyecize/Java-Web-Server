cd %~dp0../../src/http-api
call mvn clean package
call mvn deploy:deploy-file -Durl=file:../../local-repository -Dfile=target/http-1.3.jar -DgroupId=com.cyecize  -DartifactId=http -Dpackaging=jar -Dversion=1.3