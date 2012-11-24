#!/bin/sh
export JDK_DIR=/usr/java/jdk1.6.0
mkdir sysclasses
javac -classpath $JDK_DIR/lib/tools.jar -sourcepath src -d sysclasses src/com/excelsior/nothing/Main.java 
