chcp 65001
echo программа datana_siemens_v1
pause
set JAVA_HOME=d:\apps\jdk8_131\jre
set PATH=$JAVA_HOME$\bin;%PATH%
java -Dapp.dir="d:/apps/datana_siemens_v1" -jar Siemens-K4-1.0-SNAPSHOT-jar-with-dependencies.jar
