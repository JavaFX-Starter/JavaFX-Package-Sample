使用`jpackage`命令在Windows平台手动构建可执行程序和安装程序（基于gradle路径）
==========
### 命令执行环境
```
cmd
```
> 位于项目根目录

### 环境变量（已配置可跳过）
```
set JAVA_HOME=C:\CommandLineTools\Java\jdk-19
```
> 可接着输入`set JAVA_HOME`来验证是否配置成功

### 清理旧的构建（建议每一次重新构建时执行清理）
```
gradlew.bat clean
```

### 生成项目`jar`包（无法简单的通过`java -jar`执行）
```
gradlew.bat assemble
```
通过`dir build\libs`可以查看生成的文件信息
```
2023/02/06  15:31    <DIR>          .
2023/02/06  15:31    <DIR>          ..
2023/02/06  15:31           128,263 JavaFX-Package-Sample-1.0.0.jar
               1 个文件        128,263 字节
               2 个目录 66,132,144,128 可用字节
```

### 通过`java`命令来运行应用程序
> 对于一个模块化项目，首先需要指定模块路径（-p | --module-path），对于本项目可以通过`gradlew.bat printDependentJarsList`打印出项目用到的依赖并以`;`（不同平台使用的拼接字符不一致）拼接，结果如下
```
----------
C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-fxml\19\abc5441a6421ceac26bb512e96351bf641ee6004\javafx-fxml-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-web\19\6be4c7f0560267f658bd34e910dcacc231ac7681\javafx-web-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-controls\19\1227311caf209b428591fb7d2cca27e25f06f0b4\javafx-controls-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-controls\19\9f8d4f41bff27d8ac956b21e5223eaaebb7d9413\javafx-controls-19.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-media\19\50bd7cd35678978ec898cf5f9fcca631a106d29d\javafx-media-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-media\19\937b4f7b378451165c076ab2f51310a19e5a618\javafx-media-19.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-swing\19\562878eb4e5c1d5acebf1e40953c6557196e10ff\javafx-swing-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-graphics\19\8e98e30310844cd501f4e03fa3415f6d897c7215\javafx-graphics-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-graphics\19\44dbcee46b9e902629bb4e0f409b3d6e10ac8e8b\javafx-graphics-19.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-base\19\430bec8a362edb04667972905a7cb34cb165d6d\javafx-base-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-base\19\80d1a58c5345af9151f1b4b01f8eb92f86fe49ae\javafx-base-19.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\io.github.palexdev\materialfx\11.13.5\ecdf6b1455a22c8d2f0494df0a53f1923debdf9d\materialfx-11.13.5.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\io.github.palexdev\virtualizedfx\11.2.6\aa94a9c3305fc0cb3b0465e37c367877658db4a5\virtualizedfx-11.2.6.jar
----------
```
> 然后指定模块及主类限定全路径（-m | --module），对于本项目是`sample/com.icuxika.MainApp`

> 因此实际命令为（`注意实际的-p参数值在最前面拼接了对应项目本身的jar包`）
```
C:\CommandLineTools\Java\jdk-19\bin\java.exe -p ".\build\libs\JavaFX-Package-Sample-1.0.0.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-fxml\19\abc5441a6421ceac26bb512e96351bf641ee6004\javafx-fxml-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-web\19\6be4c7f0560267f658bd34e910dcacc231ac7681\javafx-web-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-controls\19\1227311caf209b428591fb7d2cca27e25f06f0b4\javafx-controls-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-controls\19\9f8d4f41bff27d8ac956b21e5223eaaebb7d9413\javafx-controls-19.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-media\19\50bd7cd35678978ec898cf5f9fcca631a106d29d\javafx-media-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-media\19\937b4f7b378451165c076ab2f51310a19e5a618\javafx-media-19.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-swing\19\562878eb4e5c1d5acebf1e40953c6557196e10ff\javafx-swing-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-graphics\19\8e98e30310844cd501f4e03fa3415f6d897c7215\javafx-graphics-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-graphics\19\44dbcee46b9e902629bb4e0f409b3d6e10ac8e8b\javafx-graphics-19.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-base\19\430bec8a362edb04667972905a7cb34cb165d6d\javafx-base-19-win.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\org.openjfx\javafx-base\19\80d1a58c5345af9151f1b4b01f8eb92f86fe49ae\javafx-base-19.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\io.github.palexdev\materialfx\11.13.5\ecdf6b1455a22c8d2f0494df0a53f1923debdf9d\materialfx-11.13.5.jar;C:\Users\icuxika\.gradle\caches\modules-2\files-2.1\io.github.palexdev\virtualizedfx\11.2.6\aa94a9c3305fc0cb3b0465e37c367877658db4a5\virtualizedfx-11.2.6.jar" -m "sample/com.icuxika.MainApp"
```
### `-p`内容过长，后面以`$ModulePath`表示，执行时需要手动替换

### 构建可执行程序（类似绿色解压版，无安装工具）
```
C:\CommandLineTools\Java\jdk-19\bin\jpackage.exe --type app-image -n JavaFXSample -p "$ModulePath" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./build/build-direct-app-package
```

### 构建安装程序
> 需要安装`WiX Toolset 3.0`以上的版本，并将其`bin`目录配置到环境变量中
> 与`构建可执行程序`步骤的主要区别是未设置`--type`参数，设置了`--resource-dir`参数和`--win-dir-chooser`等平台相关的选项

#### 基于`构建可执行程序`构建出的可执行程序目录来构建
```
C:\CommandLineTools\Java\jdk-19\bin\jpackage.exe --type msi --resource-dir .\command\resourceDir\zh_CN.wxl -n JavaFXSample --app-image ./build/build-direct-app-package/JavaFXSample --app-version 1.0.0 --dest ./build/build-direct-package --temp ./build/build-direct-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```
> 注意`--app-image`的内容来源于`构建可执行程序`的`--dest`内容

#### 重头构建
```
C:\CommandLineTools\Java\jdk-19\bin\jpackage.exe --type msi --resource-dir .\command\resourceDir\zh_CN.wxl -n JavaFXSample -p "$ModulePath" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./build/build-direct-package --temp ./build/build-direct-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```
#### 补充说明，Windows下除了`msi`格式还可以生成`exe`格式的安装程序，添加`--resource-dir`的原因是为了避免`311错误`，此错误最早在`Java14就存在`，在后面几个版本曾修复过，平时一般使用`gradle`插件的方式来进行打包，所以未能发现
##### 演示311错误以及一些奇怪的解决方法
1. 执行以下命令
```
gradlew.bat clean
gradlew.bat assemble
C:\CommandLineTools\Java\jdk-19\bin\jpackage.exe --type msi -n JavaFXSample -p "$ModulePath" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./build/build-direct-package --temp ./build/build-direct-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```
2. 命令结果
```
java.io.IOException: Command [light.exe, -nologo, -spdb, -ext, WixUtilExtension, -out, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\JavaFXSample-1.0.0.msi, -ext, WixUIExtension, -b, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\config, -sice:ICE27, -loc, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\config\MsiInstallerStrings_en.wxl, -cultures:en-us, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\main.wixobj, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\bundle.wixobj, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\ui.wixobj, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\InstallDirNotEmptyDlg.wixobj] in C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\images\win-msi.image\JavaFXSample exited with 311 code
```

3. 奇怪的解决方式
- 根据`2. 命令结果`来手动构建命令
    - 执行出错的命令为`[light.exe, -nologo, -spdb, -ext, WixUtilExtension, -out, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\JavaFXSample-1.0.0.msi, -ext, WixUIExtension, -b, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\config, -sice:ICE27, -loc, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\config\MsiInstallerStrings_en.wxl, -cultures:en-us, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\main.wixobj, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\bundle.wixobj, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\ui.wixobj, C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\InstallDirNotEmptyDlg.wixobj]`
    - 去掉`[`,`]`,`,`，得到`light.exe -nologo -spdb -ext WixUtilExtension -out C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\JavaFXSample-1.0.0.msi -ext WixUIExtension -b C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\config -sice:ICE27 -loc C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\config\MsiInstallerStrings_en.wxl -cultures:en-us C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\main.wixobj C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\bundle.wixobj C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\ui.wixobj C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\InstallDirNotEmptyDlg.wixobj`
    - 修改`-loc`和`-cultures`指向中文配置文件得到
```
light.exe -nologo -spdb -ext WixUtilExtension -out C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\JavaFXSample-1.0.0.msi -ext WixUIExtension -b C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\config -sice:ICE27 -loc C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\config\MsiInstallerStrings_zh_CN.wxl -cultures:zh-CN C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\main.wixobj C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\bundle.wixobj C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\ui.wixobj C:\Users\icuxika\IdeaProjects\JavaFX-Package-Sample\.\build\build-direct-package\temp\wixobj\InstallDirNotEmptyDlg.wixobj
```
> 执行可以获取到正确中文版本的安装包

- 指定`--vendor`参数
```
gradlew.bat clean
gradlew.bat assemble
C:\CommandLineTools\Java\jdk-19\bin\jpackage.exe --type msi -n JavaFXSample --vendor icuxika -p "$ModulePath" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./build/build-direct-package --temp ./build/build-direct-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```
> 虽然没有报错，但是安装包是英语版本

