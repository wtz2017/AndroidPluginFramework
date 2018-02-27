@Rem 把jar内的class转为dex
@echo off

@Rem ###############################################
@Rem ######【把jar内的class转为dex】################
@Rem ###############################################

@Rem 设置要处理的jar文件名
set injar=%~dp0\build\intermediates\bundles\debug\classes.jar

@Rem 设置要输出的结果文件名
set outjar=%~dp0\framework.jar

@Rem 设置转换工具目录
set dex_tool_home=D:\Software\Development\android_sdk\sdk\build-tools\android-4.4.2

echo start jar to dex...
echo target is %injar%, output is %outjar%
%dex_tool_home%\dx.bat --dex --output=%outjar% %injar%

pause 