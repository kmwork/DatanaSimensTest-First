#!/bin/sh
export JAVA_HOME=/home/lin/apps/jre1.8.0_201/
export PATH=$JAVA_HOME/bin;$PATH
java -Dapp.dir="/home/lin/apps/datana_siemens_v1" -jar Siemens-K4-1.0-SNAPSHOT-jar-with-dependencies.jar
