echo Starting deployment!

cd %~dp0../
rmdir /Q /S build-output
mkdir build-output

call %~dp0subtasks/deploy-javache-standard.bat
call %~dp0subtasks/deploy-javache-embedded.bat

cd %~dp0

echo Finished deployment!