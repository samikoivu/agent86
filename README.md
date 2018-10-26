# agent86 - a Java Agent demo

Run against WebGoat with a command line as follows (adjust java-command or path to local Java 8, and full path towebgoat.jar, all other paths are relative to the project root):

java -Xbootclasspath/a:lib\rej.jar;build\libs\agent86.jar;..\javax.servlet-api.jar -javaagent:build\libs\agent86.jar -jar webgoat-server-8.0.0.M21.jar
