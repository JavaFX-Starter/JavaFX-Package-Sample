JavaFX 非模块化打包示例（Maven）
------------------------------------------------------------
## `maven-shade-plugin`
```shell
mvn clean
mvn -Pshade package
java -jar .\target\jars\JavaFX-Package-Sample-1.0.0-shade.jar
mvn -Pshade exec:exec@image
.\target\buildImage\JavaFXSample\JavaFXSample.exe
```

## `maven-assembly-plugin`
```shell
mvn clean
mvn -Passembly package
java -jar .\target\jars\JavaFX-Package-Sample-1.0.0-jar-with-dependencies.jar
mvn -Passembly exec:exec@image
.\target\buildImage\JavaFXSample\JavaFXSample.exe
```

## 说明
`mvn package`生成的jar包在`.\target\jars\`里，可以通过`java -jar .\target\jars\JavaFX-Package-Sample-1.0.0-jar-with-dependencies.jar`运行