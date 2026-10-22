@rem ##########################################################################
@rem Gradle startup script for Windows
@rem ##########################################################################
@if "%DEBUG%"=="" @echo off
setlocal
set GRADLE_HOME=C:\Users\mamoh\.gradle\wrapper\dists\gradle-9.8.0-bin\3m7h6ceboy5k31n8kzwzuxssm\gradle-9.8.0
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
"%JAVA_HOME%\bin\java.exe" -classpath "%GRADLE_HOME%\lib\gradle-launcher-9.8.0.jar" org.gradle.launcher.GradleMain %*
