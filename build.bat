@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
set "ANDROID_HOME=C:\Users\Arman\AppData\Local\Android\Sdk"
set "ANDROID_USER_HOME=C:\Users\Arman\.android"
set "ANDROID_PREFS_ROOT="
call gradlew.bat assembleDebug --no-daemon
