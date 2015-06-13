#!/bin/sh
unset JAVA_TOOL_OPTIONS
reset
/home/vns/apps/java/jdk/bin/java -jar /home/vns/workspace/gtranslator/target/gtranslator.jar --prop-path='/home/vns/workspace/gtranslator/settings.xml'
