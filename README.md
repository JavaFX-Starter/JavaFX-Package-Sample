JavaFX 非模块化打包示例（Maven）
------------------------------------------------------------
```shell
mvn clean
mvn package
cp .\target\JavaFX-Package-Sample-1.0.0.jar .\target\alternateLocation\
mvn exec:exec@non-modular-image
.\target\buildImage\JavaFXSample\JavaFXSample.exe
```