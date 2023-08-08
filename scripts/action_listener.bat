rem @echo off





:start

if not exist "to_play.txt" (
goto wait
)

set /p TOPLAY=< to_play.txt
if not [%TOPLAY%]==[] (
del to_play.txt
start /high /MAX cmd /C ""C:\Program Files\SMPlayer\smplayer.exe" -fullscreen -ontop %TOPLAY%"
)


:wait
timeout 3
goto start
