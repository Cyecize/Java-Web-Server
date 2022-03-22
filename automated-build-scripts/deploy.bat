echo Starting deployment!

cd %~dp0../
rmdir /Q /S build-output
mkdir build-output

call %~dp0subtasks/deploy-javache-embedded.bat
call %~dp0subtasks/deploy-javache-standard.bat

cd %~dp0

echo Finished deployment!