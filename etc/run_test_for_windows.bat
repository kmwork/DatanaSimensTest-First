chcp 65001
echo тест
pause
set JAVA_HOME=f:\apps\jdk8_131\jre
set PATH=%JAVA_HOME%\bin;%PATH%
java -Dapp.dir="f:/apps/datana_siemens_v1" -jar Siemens-K4-1.0-SNAPSHOT-jar-with-dependencies.jar
