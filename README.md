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
## 构建 native 动态库
### 生成 JNI 头文件
> 不修改可以不执行
```
mvn exec:exec@jni-generate
```

### 构建
```shell
.\BuildNative.ps1
```

#### 控制台标准输出乱码
```
java -D"sun.stdout.encoding"=UTF-8 -D"sun.stderr.encoding"=UTF-8 -jar .\target\jars\JavaFX-Package-Sample-1.0.0-shade.jar
```
