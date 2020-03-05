#!/bin/sh
export JAVA_HOME=/home/lin/apps/jre1.8.0_201/
export PATH=$JAVA_HOME/bin;$PATH
java -jar Siemens-K4-1.0-SNAPSHOT-jar-with-dependencies.jar -Dapp.dir="/home/lin/apps/datana_siemens_v1"
