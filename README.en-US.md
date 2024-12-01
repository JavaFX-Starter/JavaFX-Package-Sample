<h1 align="center">JavaFX-Package-Sample</h1>
<p align="center">JavaFX Project Packaging Example</p>
<p align="center">English | <a href="README.md">中文</a></p>

## Branches
- `java21` Package a `modular` JavaFX project using `Maven`, `Gradle`, and `GraalVM`.
- `java21-non-modular` [Link](https://github.com/JavaFX-Starter/JavaFX-Package-Sample/tree/java21-non-modular) Package a `non-modular` JavaFX project using `Maven`.
- `java22-win32` [Link](https://github.com/JavaFX-Starter/JavaFX-Package-Sample/tree/java22-win32) A JavaFX project that implements Windows global keyboard event listening by utilizing the `Foreign Function and Memory (FFM) API`.

## Requirements
PowerShell:
```
$env:JAVA_HOME = "C:\CommandLineTools\Java\jdk-21\"
```
```
$env:GRAALVM_HOME = "C:\CommandLineTools\Java\graalvm-jdk-21.0.2+13.1\"
```

## Getting Started
Gradle:
```
.\gradlew.bat jpackageImage
.\gradlew.bat jpackage
```

Maven:
```
mvn clean
mvn package
mvn exec:exec@image
mvn exec:exec@installer
```

GraalVM:
```
.\gradlew.bat nativeRunAgent
.\gradlew.bat nativeBuild
.\gradlew.bat nativeRun
```
```
mvn -Pwin gluonfx:build
mvn -Pwin gluonfx:run

mvn -Pmac gluonfx:build
mvn -Pmac gluonfx:run
```