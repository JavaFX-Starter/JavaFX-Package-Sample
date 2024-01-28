JavaFX 非模块化打包示例（Maven）
------------------------------------------------------------
```shell
mvn clean
mvn package
mvn exec:exec@image
.\target\buildImage\JavaFXSample\JavaFXSample.exe
```

## 说明
`mvn package`生成的jar包在`.\target\jars\`里，可以通过`java -jar .\target\jars\JavaFX-Package-Sample-1.0.0-jar-with-dependencies.jar`运行