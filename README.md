# agent86 - a Java Agent demo [![Build Status](https://travis-ci.com/samikoivu/agent86.svg?branch=master)](https://travis-ci.com/samikoivu/agent86)

Download
========

agent86: https://github.com/samikoivu/agent86/releases/download/rel1/agent86.jar

reJ bytecode API: https://github.com/samikoivu/rej/releases/download/v0.8/rej.jar

Java servlet API: https://maven.java.net/content/repositories/releases/javax/servlet/javax.servlet-api/4.0.0/javax.servlet-api-4.0.0.jar

WebGoat: https://github.com/WebGoat/WebGoat/releases

Running
=======

Run against WebGoat with a command line as follows (adjust java-command or path to local Java 8, and full path to webgoat.jar, rej.jar and agent86.jar

java -Xbootclasspath/a:rej.jar;agent86.jar;javax.servlet-api.jar -javaagent:agent86.jar -jar webgoat-server-8.0.0.M21.jar

Logging
=======

Requests will be logged in a text-file in the current directory, in agentlog.txt, as an example:

===================================================
Request URI: /WebGoat/login
Request ID: b3f068f98dcb40f3
Duration: 1588ms
Unique strings created: 152114
Classes loaded: 299

The Request ID will be added as the HTTP Header of the response.
