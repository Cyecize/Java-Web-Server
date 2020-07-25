echo Start Process of rebuilding dependencies!

call %~dp0subtasks/clear-maven-dependencies.bat

call %~dp0subtasks/rebuild-http-api.bat
call %~dp0subtasks/rebuild-solet-api.bat
call %~dp0subtasks/rebuild-server-api.bat
call %~dp0subtasks/rebuild-toyote.bat
call %~dp0subtasks/rebuild-broccolina.bat
call %~dp0subtasks/rebuild-server.bat

echo Finished rebuilding dependencies!