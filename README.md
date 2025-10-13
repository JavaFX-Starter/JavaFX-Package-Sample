JavaFX 调用 Win32 API
------------------------------------------------------------

## Jar 运行

```
mvn clean package
java -jar .\target\jars\JavaFX-Package-Sample-1.0.0-shade.jar
```

## 生成 exe

```
mvn -Pwin exec:exec@image
```

## 生成安装包

```
mvn -Pwin clean package exec:exec@image
.\BuildInstaller.ps1
```

## 构建 native 动态库

### 生成 JNI 头文件

> 没有新增或修改native函数可以不执行

```
mvn -Pjni clean compile
```

### 构建

```
.\BuildNative.ps1
```

## 控制台标准输出乱码

```
java -D"sun.stdout.encoding"=UTF-8 -D"sun.stderr.encoding"=UTF-8 -jar .\target\jars\JavaFX-Package-Sample-1.0.0-shade.jar
```

## 生成 AOT 文件

> AOT 与 jpackage 还不能很好的一起使用，jvm 参数不同，AOT 文件就不可用，目前只通过 jar 的方式试一试

```
java -XX:AOTCacheOutput=app.aot -jar .\target\jars\JavaFX-Package-Sample-1.0.1-shade.jar
java -XX:AOTCache=app.aot -jar .\target\jars\JavaFX-Package-Sample-1.0.1-shade.jar

java --enable-native-access=ALL-UNNAMED -XX:AOTCacheOutput=app.aot -jar .\target\jars\JavaFX-Package-Sample-1.0.1-shade.jar
java --enable-native-access=ALL-UNNAMED -XX:AOTCache=app.aot -jar .\target\jars\JavaFX-Package-Sample-1.0.1-shade.jar
```
