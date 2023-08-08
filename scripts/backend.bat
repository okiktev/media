@echo off

call setenv.bat

cd %BASE_DIR%

call gradlew.bat bootRun

cd %CUR_DIR%



