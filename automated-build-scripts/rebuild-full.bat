cd ../
rmdir /Q /S build-output
mkdir build-output

call %~dp0subtasks/deploy-javache-standard.bat

cd %~dp0