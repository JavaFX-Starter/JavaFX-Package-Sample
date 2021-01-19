JavaFX 打包示例（Windows下的演示，其他平台自行替换）
------------------------------------------------------------

### 准备工作（PowerShell中设置的临时环境变量对mvn无效，在cmd中设置的有效）

- ```set JAVA_HOME="C:/CommandLineTools/Java/jdk-15"```
- ```mvn package```
- ```gradlew assemble```

> 以下命令都是Java15的版本

### 直接执行

```
java -p "./target/JavaFX-Package-Sample-1.0-SNAPSHOT.jar;C:/CommandLineTools/Java/javafx-sdk-15.0.1/lib" -m "sample/com.icuxika.MainApp"
```

### 查询程序依赖的模块

```
jdeps --print-module-deps --ignore-missing-deps --module-path "C:/CommandLineTools/Java/javafx-sdk-15.0.1/lib" ./target/JavaFX-Package-Sample-1.0-SNAPSHOT.jar
```

### 构建

#### 一、由```JPackage```直接构建

> 执行以下命令，不出意外的话，会得到一个WIX TOOLSET 311的错误（Windows区域与语言为中文的情况下）

```
jpackage -n JavaFXSample -p "./target/JavaFX-Package-Sample-1.0-SNAPSHOT.jar;C:/CommandLineTools/Java/javafx-jmods-15.0.1" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./target/build-direct-package --temp ./target/build-direct-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```

> 若是Windows区域语言为美国英语的话，不会出现此错误，需要手动指定下区域配置，来进行安装包制作，[相关issue](https://github.com/beryx/badass-runtime-plugin/issues/71) （此问题仅限于Windows平台，注意上一条命令中指定的temp目录）

```
light -nologo -spdb -cultures:zh-CN -ext WixUtilExtension -out ./target/build-direct-package/temp/images/win-exe.image/JavaFXSample-1.0.msi -sice:ICE27 -ext WixUIExtension -loc ./target/build-direct-package/temp/config/MsiInstallerStrings_zh_CN.wxl, -b, ./target/build-direct-package/temp/config ./target/build-direct-package/temp/wixobj/main.wixobj ./target/build-direct-package/temp/wixobj/bundle.wixobj
```

> 上面两条命令执行后，会得到安装程序，下面演示只生成exe及其依赖环境

```
jpackage --type app-image -n JavaFXSample -p "./target/JavaFX-Package-Sample-1.0-SNAPSHOT.jar;C:/CommandLineTools/Java/javafx-jmods-15.0.1" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./target/build-direct-app-package
```

> 执行`三`

#### 二、使用```JLink```自定义模块后，再使用```JPackage```来构建

> 首先使用```JLink```构建出bat及其依赖环境（通过```mvn javafx:jlink```或```gradle jpackageImage```也会得到这一步的结果）

```
jlink --output ./target/build-link -p "./target/JavaFX-Package-Sample-1.0-SNAPSHOT.jar;C:/CommandLineTools/Java/javafx-jmods-15.0.1" --add-modules java.base,javafx.controls,sample --launcher JavaFXSample="sample/com.icuxika.MainApp" --no-header-files --no-man-pages --compress=2 --strip-debug
```

> 然后基于```JLink```构建出来的目录来进行```JPackage```构建出exe及其依赖环境

```
jpackage --type app-image -n JavaFXSample -m "sample/com.icuxika.MainApp" --icon "./src/main/resources/application.ico" --runtime-image "./target/build-link/" --dest "./target/build-package"
```

> 执行`三`

#### 三、对构建好的exe及其依赖环境进行打包

> 此命令与`一`中第一条是同一个结果（当前命令使用的是`二`中构建出的目录）

```
jpackage -n JavaFXSample --app-image ./target/build-package/JavaFXSample --app-version 1.0.0 --dest ./target/build-link-package --temp ./target/build-link-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```

> 错误处理，[相关issue](https://github.com/beryx/badass-runtime-plugin/issues/71) （此问题仅限于Windows平台，注意上一条命令中指定的temp目录）

```
light -nologo -spdb -cultures:zh-CN -ext WixUtilExtension -out ./target/build-link-package/temp/images/win-exe.image/JavaFXSample-1.0.msi -sice:ICE27 -ext WixUIExtension -loc ./target/build-link-package/temp/config/MsiInstallerStrings_zh_CN.wxl, -b, ./target/build-link-package/temp/config ./target/build-link-package/temp/wixobj/main.wixobj ./target/build-link-package/temp/wixobj/bundle.wixobj
```

### 对比（十分不严谨）

- `JPackage`直接构建：`135 MB`
- 先执行`JLink`然后执行`JPackage`构建：`106 MB`
- 通过Maven插件：`70.5 MB`

### Gradle 与 Maven 使用

|命令|Maven|Gradle|
|----|----|----|
|生成bat|```mvn javafx:jlink```|```gradle jlink```|
|生成exe|当前插件需要手动|```gradle jpackageImage```|
|制作安装包|看上面|```gradle jpackage```（与`三`有着同样的问题）|


