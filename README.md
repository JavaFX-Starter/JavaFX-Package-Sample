JavaFX 打包示例（Windows下的演示，其他平台自行替换）
------------------------------------------------------------

### 当前为Java16版本，Java15版本请转到[主分支](https://github.com/icuxika/JavaFX-Package-Sample/tree/master)

### 若你实在没有办法处理非模块化的第三方依赖及分裂包相关的模块化问题，可以参考[KtFX-Lets-Plot](https://github.com/icuxika/KtFX-Lets-Plot/tree/non-modular) 的`non-modular`分支实现，使用了[The Badass Runtime Plugin](https://badass-runtime-plugin.beryx.org/releases/latest/) 插件来对非模块化项目进行构建，`仅支持Gradle`，体积会大不少

### 准备工作

- 命令行环境：```set JAVA_HOME="C:\CommandLineTools\Java\jdk-16"```，PowerShell中设置的临时环境变量对mvn无效，在cmd中设置的有效
- IDEA：
    - Gradle：Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> 指定Gradle JVM为Java16
    - Edit Configurations：JVM options -> （方式二需要在此添加参数）

### Gradle Task（方式一）

使用gradle自定义任务拼接命令进行打包（不要变动项目的gradle版本设置，同时请确认gradle运行在java16环境下）

- 构建EXE镜像 -> ```gradlew package2Image```
- 构建MSI安装包 -> ```gradlew package2Installer```

### Badass JLink Plugin（方式二，只支持Gradle，当前插件制作出来的`MSI安装包`会比`方式一`和`方式三`大十几兆，但是`EXE镜像`会小不少）

- 构建EXE镜像 -> ```gradlew jpackageImage```
- 构建安装包（MSI、EXE） -> ```gradlew jpackage```

### Maven Exec（方式三，Maven不熟悉）

本方式目前的配置只够建出了镜像，如果要构建出安装包需要自行调整和补充插件`org.codehaus.mojo:exec-maven-plugin`的配置项，包括`--icon`参数在`macOS`平台上需要改成`icns`文件的路径

- 构建`jar`包（本步骤会自动拷贝项目所需第三方依赖到`target/alternateLocation`目录下） -> ```mvn package```
- 构建`jpackage`命令执行打包 -> ```mvn exec:exec```

### 手动构建（方式四，为方式一的手动版）

- 下面的命令都是基于`gradle assemble(jar)`构建出的jar包路径（build目录）来执行，如果使用`mvn package`需要自行替换对应的jar包路径（target目录）。
- maven版本的相关插件正常使用（`mvn javafx:run`、`mvn javafx:jlink`）
- 下述命令切换到其他平台应该通用（未测试）。

#### 执行（以下命令中`-p`参数的值，可以使用```gradle printDependentJarsList```的结果来替换）

```
C:\CommandLineTools\Java\jdk-16\bin\java.exe --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix -p ".\build\libs\JavaFX-Package-Sample-1.0.0.jar;C:\CommandLineTools\Java\javafx-sdk-16\lib;C:\Users\icuxika\.m2\repository\com\jfoenix\jfoenix\9.0.10\jfoenix-9.0.10.jar" -m "sample/com.icuxika.MainApp"
```

#### 构建出安装包（WIX TOOLSET）

```
C:\CommandLineTools\Java\jdk-16\bin\jpackage.exe -n JavaFXSample --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix" -p ".\build\libs\JavaFX-Package-Sample-1.0.0.jar;C:\CommandLineTools\Java\javafx-jmods-16;C:\Users\icuxika\.m2\repository\com\jfoenix\jfoenix\9.0.10\jfoenix-9.0.10.jar" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./build/build-direct-package --temp ./build/build-direct-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```

#### 构建出镜像

```
C:\CommandLineTools\Java\jdk-16\bin\jpackage.exe --type app-image -n JavaFXSample --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix" -p ".\build\libs\JavaFX-Package-Sample-1.0.0.jar;C:\CommandLineTools\Java\javafx-jmods-16;C:\Users\icuxika\.m2\repository\com\jfoenix\jfoenix\9.0.10\jfoenix-9.0.10.jar" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./build/build-direct-app-package
```

### 【补充】利用`mvn javafx:jlink`构建出的镜像来构建可执行程序（`pom.xml`插件里已经指定了`--add-exports`等参数，但是此命令依旧需要加，java15时却是不需要的）

> 生成exe

```
C:\CommandLineTools\Java\jdk-16\bin\jpackage.exe --type app-image -n JavaFXSample --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix" -m "sample/com.icuxika.MainApp" --icon "./src/main/resources/application.ico" --runtime-image "./target/build-link/" --dest "./target/build-package"
```

> 构建出安装包

```
C:\CommandLineTools\Java\jdk-16\bin\jpackage.exe -n JavaFXSample --app-image ./target/build-package/JavaFXSample --app-version 1.0.0 --dest ./target/build-link-package --temp ./target/build-link-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```


