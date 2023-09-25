使用`jpackage`命令在Windows平台手动构建可执行程序和安装程序
==========

## 生成项目`jar`包（无法简单的通过`java -jar`执行）
`gradle` -> `.\build\libs\JavaFX-Package-Sample-1.0.0.jar`
```
.\gradlew.bat clean
.\gradlew.bat assemble
```
`maven` -> `.\target\JavaFX-Package-Sample-1.0.0.jar`
```
mvn clean
mvn package
```
> `mvn package`执行时会将项目依赖的jar包拷贝到`.\target\alternateLocation\`

## 通过`java`命令来运行应用程序
`gradle`

先执行`.\gradlew.bat copyDependencies`将项目依赖的jar包拷贝到`.\build\modules\`
```shell
C:\CommandLineTools\Java\jdk-21\bin\java.exe -p ".\build\libs\;.\build\modules\" -m "sample/com.icuxika.MainApp"
```
`maven`
```shell
C:\CommandLineTools\Java\jdk-21\bin\java.exe -p ".\target\JavaFX-Package-Sample-1.0.0.jar;.\target\alternateLocation\" -m "sample/com.icuxika.MainApp"
```

## 通过`jdeps`命令可以显示项目依赖的模块
`gradle`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jdeps.exe --print-module-deps --ignore-missing-deps --module-path .\build\modules\ .\build\libs\JavaFX-Package-Sample-1.0.0.jar
```
> 输出：MaterialFX,java.base
 
`maven`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jdeps.exe --print-module-deps --ignore-missing-deps --module-path .\target\alternateLocation\ .\target\JavaFX-Package-Sample-1.0.0.jar
```
> 输出：MaterialFX,java.base

## 通过`jlink`构建出`.bat`形式的可执行程序
`gradle` -> `.\build\jlink-build-dir\bin\JavaFX-Package-Sample.bat`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jlink.exe --module-path ".\build\libs\;.\build\modules\" --add-modules java.base,sample --launcher JavaFX-Package-Sample=sample/com.icuxika.MainApp --compress=zip-9 --no-header-files --no-man-pages --strip-debug --output .\build\jlink-build-dir
```

`maven` -> `.\target\jlink-build-dir\bin\JavaFX-Package-Sample.bat`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jlink.exe --module-path ".\target\JavaFX-Package-Sample-1.0.0.jar;.\target\alternateLocation\" --add-modules java.base,sample --launcher JavaFX-Package-Sample=sample/com.icuxika.MainApp --compress=zip-9 --no-header-files --no-man-pages --strip-debug --output .\target\jlink-build-dir
```

> 通过`jlink`构建出的文件夹`jlink-build-dir`已经可以压缩一下发到其他电脑上运行了，`.bat`脚本也有许多方式能够直接转为`.exe`，目前`jlink-build-dir`目录的大小为`89.7 MB`

## jpackage 使用一：以`jlink`命令构建出的目录`jlink-build-dir`为基础
`gradle` -> `.\build\jpackage-build-dir\JavaFXSample\JavaFXSample.exe`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --type app-image -n JavaFXSample -m "sample/com.icuxika.MainApp" --runtime-image .\build\jlink-build-dir\ --icon .\src\main\resources\application.ico --app-version 1.0.0 --dest .\build\jpackage-build-dir\
```
`maven` -> `.\target\jpackage-build-dir\JavaFXSample\JavaFXSample.exe`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --type app-image -n JavaFXSample -m "sample/com.icuxika.MainApp" --runtime-image .\target\jlink-build-dir\ --icon .\src\main\resources\application.ico --app-version 1.0.0 --dest .\target\jpackage-build-dir\
```

> 目前`jpackage-build-dir`大小为`90.1 MB`

## jpackage 使用二：不以`jlink`构建结果为基础
`gradle` -> `.\build\jpackage-direct-build-dir\JavaFXSample\JavaFXSample.exe`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --type app-image -n JavaFXSample --module-path ".\build\libs\;.\build\modules\" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest .\build\jpackage-direct-build-dir
```
`maven` -> `.\target\jpackage-direct-build-dir\JavaFXSample\JavaFXSample.exe`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --type app-image -n JavaFXSample --module-path ".\target\JavaFX-Package-Sample-1.0.0.jar;.\target\alternateLocation\" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest .\target\jpackage-direct-build-dir
```

> 目前`jpackage-direct-build-dir`大小为`182 MB`

## 以下命令是构建Windows上`msi`格式的安装程序，需要配置[WiX Toolset v3.11.2](https://github.com/wixtoolset/wix3/releases/tag/wix3112rtm)的`bin`目录到环境变量`PATH`中

## jpackage 使用三：以`jpackage 使用二`的构建目录`jpackage-direct-build-dir`为基础
`gradle` -> `.\build\jpackage-installer-dir\JavaFXSample-1.0.msi`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --type msi -n JavaFXSample --app-image .\build\jpackage-direct-build-dir\JavaFXSample\ --dest .\build\jpackage-installer-dir --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```
`maven` -> `.\target\jpackage-installer-dir\JavaFXSample-1.0.msi`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --type msi -n JavaFXSample --app-image .\target\jpackage-direct-build-dir\JavaFXSample\ --dest .\target\jpackage-installer-dir --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```

> 目前`JavaFXSample-1.0.msi`的大小为`65.9 MB`

> 在jdk19、jdk20等版本有时候构建会遇到构建出的安装包是英文版本或者`exited with 311 code`的错误，请切换到`java20`分支此文件查看相应的解决方法，当前jdk21没有遇到这些问题

## jpackage 使用四：不以`jpackage 使用二`的构建结果为基础
`gradle` -> `.\build\jpackage-direct-installer-dir\JavaFXSample-1.0.0.msi`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --type msi -n JavaFXSample --module-path ".\build\libs\;.\build\modules\" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest .\build\jpackage-direct-installer-dir\ --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```
`maven` -> `.\target\jpackage-direct-installer-dir\JavaFXSample-1.0.0.msi`
```shell
C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --type msi -n JavaFXSample --module-path ".\target\JavaFX-Package-Sample-1.0.0.jar;.\target\alternateLocation\" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest .\target\jpackage-direct-installer-dir\ --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```

> 目前`JavaFXSample-1.0.msi`的大小为`65.9 MB`


## 最后
`jlink`和`jpackage`两条命令最主要的使用方式和关键的参数都已经在上面了，不过本项目只演示了作为一个模块化项目时应用的方式，但是`jpackage`是支持为一个非模块化的可以通过`java -jar`运行的`jar`包生成对应的exe的。
Java模块化尤其是这种纯命令行的方式难以处理很多情况比如非模块化的依赖，甚至同时支持模块化导入和兼容非模块化的依赖有时也会遇到问题，我已经有好长时间没写过`javafx`相关的代码，`jpackage`除了几个玩具项目几乎也没怎么用到过，以这点经验来说，还是找一个合适的插件为好，更简单的方式是在grdle中使用[The Badass Runtime Plugin](https://badass-runtime-plugin.beryx.org/releases/latest/)这个插件来对非模块化项目打包，这样的话项目中不用维护一个`module-info.java`文件，也不用考虑在引入新的依赖各种模块的冲突问题了

使用 mvn，配置一下阿里仓库的镜像就几乎不会有网络问题了，但是更好用的maven插件你可能需要多上上`Google`才能发现

使用 gradle，最大的问题就是你需要学会翻墙，并在IDEA中配置代理，命令行中 gradlew.bat 命令结尾添加`-D"https.proxyHost"=127.0.0.1 -D"https.proxyPort"=7890`
