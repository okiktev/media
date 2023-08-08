
@echo off

set esc=%1
set esc=%esc:"=%
set esc1=%esc:(=^^^^(%
set esc2=%esc1:)=^^)%

echo "%esc2%">to_play.txt

