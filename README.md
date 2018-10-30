# agent86 - a Java Agent demo [![Build Status](https://travis-ci.com/samikoivu/agent86.svg?branch=master)](https://travis-ci.com/samikoivu/agent86)

Download WebGoat: XXX

Download

agent86: https://github.com/samikoivu/agent86/releases/download/rel1/agent86.jar

reJ bytecode API: https://github.com/samikoivu/rej/releases/download/v0.7/rej.jar

Java servlet API: https://maven.java.net/content/repositories/releases/javax/servlet/javax.servlet-api/4.0.0/javax.servlet-api-4.0.0.jar

WebGoat: https://github.com/WebGoat/WebGoat/releases

Run against WebGoat with a command line as follows (adjust java-command or path to local Java 8, and full path to webgoat.jar, rej.jar and agent86.jar

java -Xbootclasspath/a:rej.jar;agent86.jar;javax.servlet-api.jar -javaagent:agent86.jar -jar webgoat-server-8.0.0.M21.jar
