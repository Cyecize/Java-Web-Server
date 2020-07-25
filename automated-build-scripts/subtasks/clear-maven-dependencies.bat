echo Clearing local dependencies from '.m2' directory

cd %~dp0../../
rmdir /Q /S local-repository

cd src
call mvn dependency:purge-local-repository -DmanualInclude=com.cyecize:http,com.cyecize:solet,com.cyecize:broccolina,com.cyecize:javache,com.cyecize:javache-api,com.cyecize:toyote