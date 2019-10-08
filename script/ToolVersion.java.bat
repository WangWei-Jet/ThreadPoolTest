@echo on
::  进入盘符
%~d0
::进入目录
CD %~dp0
subwcrev .. .\ToolVersion.java.tmpl ..\src\com\xiaoma\mytest\ToolVersion.java

