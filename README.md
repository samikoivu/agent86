# agent86 - a Java Agent demo

Run against WebGoat with a command line as follows:

java -Xbootclasspath/a:lib\rej.jar;build\libs\agent86.jar;..\javax.servlet-api.jar -javaagent:build\libs\agent86.jar -cp ..\webgoat-server-8.0.0.M21.jar org.springframework.boot.loader.JarLauncher
