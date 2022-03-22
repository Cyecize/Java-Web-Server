:: Use this file to run javache. Check out the latest build to see where to place the file.
@echo off

set libraries=.
cd bin

:: for loop iterating all files in the bin folder and concatenating a string to pass to java
SETLOCAL ENABLEDELAYEDEXPANSION
for %%i in (*) do (
	set libraries=!libraries!;../bin/%%i
)

cd ../

cd server
:: java  -cp ".;../bin/javache-api-1.3.jar;../bin/magic-injector-1.2.jar" com.cyecize.StartUp
java  -cp "%libraries%" com.cyecize.StartUp
PAUSE