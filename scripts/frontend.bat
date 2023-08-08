@echo off

call setenv.bat

cd %APP_DIR%

call ng serve

cd %CUR_DIR%
