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

## 程序更新功能

### 简单的更新服务器

jdk 17 以上版本自带的 `jwebserver` 可以作为简单的测试更新服务器

```
jwebserver.exe -p 8080 -d .
```

目录结构如下，在`D:\Server`目录下执行上述命令

```
D:\Server
└───JavaFXSample
    │   latest.json
    └───1.0.4
        │   AppUpdateTool.exe
        │   auto-update-helper.exe 
        │   JavaFXSample.exe
        │   update-index.json
        ├───app
        └───runtime
            ├───bin
            ├───conf
            ├───legal
            └───lib
```

其中`latest.json`存储最新版本信息

```
{
    "version": "1.0.4",
    "updateIndexUrl": "1.0.4/update-index.json"
}
```

程序检查更新时，会先获取`latest.json`文件，判断是否有新版本。如果有新版本，会根据`updateIndexUrl`(由于目前文件名称固定，实际代码只看
`version`)获取更新索引文件`update-index.json`，
同时本地也会生成一份`update-index.json`，将两份文件进行比较得到需要新增、更新或删除的文件集合，`update-index.json`
存储了所有被观察是否需要更新的文件的信息，包括文件名、文件大小、文件校验值等，但不包括`runtime(Java运行时)`，
`auto-update-helper.exe(Windows程序更新助手)`，
之后如果用户点击`一键升级`，会将上述结果保存到`$env:LOCALAPPDATA\JavaFXPackageSample\update\latest.json`
，同时下载所有需要的新版本文件，之后退出程序同时启动`auto-update-helper.exe`，
`auto-update-helper.exe`会读取`$env:LOCALAPPDATA\JavaFXPackageSample\update\latest.json`来执行文件的复制或删除操作完成更新然后重新启动程序。

## 模块化构建

模块化应用程序构建结果占用空间相比非模块化应用程序小很多，210 MB -> 135 MB
MB，但模块化应用程序打包时目录结构与非模块化应用程序不一致，所有模块都被写入一个单独的特别大(110 MB)的
`.\target\buildImage\JavaFXSample\runtime\lib\modules`文件中，部分更新时需要替换的文件尺寸占比太大。
因此，本项目的应用程序更新逻辑对于模块化应用程序不那么适用了，相关代码也并未适配，
但依旧可以基于此使用制作一个不那么大的应用程序安装包，新版本直接发布最新安装包重新安装即可。

```
.\BuildModular.ps1
.\BuildInstaller.ps1
```