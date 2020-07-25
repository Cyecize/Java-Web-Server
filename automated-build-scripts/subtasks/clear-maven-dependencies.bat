cd %~dp0../../src

call mvn dependency:purge-local-repository -DmanualInclude=com.cyecize:http,com.cyecize:solet,com.cyecize:broccolina,com.cyecize:javache,com.cyecize:javache-api,com.cyecize:toyote