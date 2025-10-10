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
