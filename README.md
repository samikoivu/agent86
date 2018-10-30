# agent86 - a Java Agent demo [![Build Status](https://travis-ci.com/samikoivu/agent86.svg?branch=master)](https://travis-ci.com/samikoivu/agent86)

Download WebGoat: XXX

Download agent86, reJ and Java servlet support.

Run against WebGoat with a command line as follows (adjust java-command or path to local Java 8, and full path to webgoat.jar, rej.jar and agent86.jar

java -Xbootclasspath/a:rej.jar;agent86.jar;javax.servlet-api.jar -javaagent:agent86.jar -jar webgoat-server-8.0.0.M21.jar
