chcp 65001
echo тест
pause
set JAVA_HOME=d:\apps\jdk8_131\jre
set PATH=$JAVA_HOME$\bin;%PATH%
java -jar Siemens-K4-1.0-SNAPSHOT-jar-with-dependencies.jar -Dapp.dir="d:/apps/datana_siemens_v1"
