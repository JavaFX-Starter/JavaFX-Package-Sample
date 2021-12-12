JavaFX 打包示例（Windows下的演示，其他平台自行替换）
------------------------------------------------------------

### 当前为Java17版本，Java15版本请转到[主分支](https://github.com/icuxika/JavaFX-Package-Sample/tree/master)

### 若你实在没有办法处理非模块化的第三方依赖及分裂包相关的模块化问题，可以参考使用[The Badass Runtime Plugin](https://badass-runtime-plugin.beryx.org/releases/latest/)

插件来对非模块化项目进行构建

- [IMFrameworkFX](https://github.com/icuxika/IMFrameworkFX) （Java）
- [KtFX-Lets-Plot](https://github.com/icuxika/KtFX-Lets-Plot/tree/non-modular) 的`non-modular`分支实现 （Kotlin）

### 准备工作

- 命令行环境：```set JAVA_HOME="C:\CommandLineTools\Java\jdk-17"```，PowerShell中设置的临时环境变量对mvn无效，在cmd中设置的有效
- IDEA：
  - Gradle：Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> 指定Gradle JVM为Java17
- Windows：需要安装 [WIX TOOLSET](https://wixtoolset.org/) ，并将其bin目录添加到环境变量中
- 其他平台缺失的包一般可以根据提示通过包管理器直接安装

### 方式一（Gradle Task）

> 当前配置下，此方式（包括Gradle方式的手动版）存在问题，原因是，使用`com.gluonhq.gluonfx-gradle-plugin`插件需要依赖`org.openjfx.javafxplugin`插件，这种情况会产生两个依赖，
> 如`javafx-base`与`javafx:base:mac`，然后Java默认的模块化系统读取的时候就会从两个包中发现两个一样的模块从而产生错误。解决方式：一、方式二不受此问题影响；二：不需要使用GraalVm的情况下，注释掉
> `com.gluonhq.gluonfx-gradle-plugin`与`org.openjfx.javafxplugin`插件，然后注释掉对应的`javafx`依赖配置块与`gluonfx`配置块，最后手动在`dependencies`中设置`JavaFX`的依赖。

使用gradle自定义任务拼接命令进行打包（不要变动项目的gradle版本设置，同时请确认gradle运行在java17环境下）

- 构建镜像 -> ```gradlew package2Image```
- 构建安装包 -> ```gradlew package2Installer```

### 方式二（Badass JLink Plugin，只支持Gradle）

- 构建镜像 -> ```gradlew jpackageImage```
- 构建安装包 -> ```gradlew jpackage```

### 方式三（Maven Exec）

> 平台图标文件类型
> - `Windows`: ico
> - `macOS`: icns
> - `Linux`: png
>
> `resources`目录下已经存在了各个图标文件，需要配置`org.codehaus.mojo:exec-maven-plugin`插件在不同平台下`--icon`参数的值，如：`${project.basedir}/src/main/resources/application.icns`

- 构建`jar`包（本步骤会自动拷贝项目所需第三方依赖到`target/alternateLocation`目录下） -> ```mvn package```
- 构建镜像 -> ```mvn exec:exec@image```
- 构建安装包 -> ```mvn exec:exec@installer```

### 方式四（手动构建，为方式一的手动版）

- 下面的命令都是基于`gradle assemble(jar)`构建出的jar包路径（build目录）来执行，如果使用`mvn package`需要自行替换对应的jar包路径（target目录）。
- maven版本的相关插件正常使用（`mvn javafx:run`、`mvn javafx:jlink`）
- 下述命令切换到其他平台请自行替换，此项目在macOS、Windows、Deepin上运行过，相关平台样式的命令都存在。

#### 执行（以下命令中`-p`参数的值，可以使用```gradle printDependentJarsList```的结果来替换）

```
C:\CommandLineTools\Java\jdk-17\bin\java.exe --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix -p ".\build\libs\JavaFX-Package-Sample-1.0.0.jar;C:\CommandLineTools\Java\javafx-sdk-17\lib;C:\Users\icuxika\.m2\repository\com\jfoenix\jfoenix\9.0.10\jfoenix-9.0.10.jar" -m "sample/com.icuxika.MainApp"
```

#### 构建出安装包（WIX TOOLSET）

```
C:\CommandLineTools\Java\jdk-17\bin\jpackage.exe -n JavaFXSample --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix" -p ".\build\libs\JavaFX-Package-Sample-1.0.0.jar;C:\CommandLineTools\Java\javafx-jmods-17;C:\Users\icuxika\.m2\repository\com\jfoenix\jfoenix\9.0.10\jfoenix-9.0.10.jar" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./build/build-direct-package --temp ./build/build-direct-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```

#### 构建出镜像

```
C:\CommandLineTools\Java\jdk-17\bin\jpackage.exe --type app-image -n JavaFXSample --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix" -p ".\build\libs\JavaFX-Package-Sample-1.0.0.jar;C:\CommandLineTools\Java\javafx-jmods-17;C:\Users\icuxika\.m2\repository\com\jfoenix\jfoenix\9.0.10\jfoenix-9.0.10.jar" -m "sample/com.icuxika.MainApp" --icon ./src/main/resources/application.ico --app-version 1.0.0 --dest ./build/build-direct-app-package
```

### 【补充】利用`mvn javafx:jlink`构建出的镜像来构建可执行程序（`pom.xml`插件里已经指定了`--add-exports`等参数，但是此命令依旧需要加，java15时却是不需要的）

> 生成exe

```
C:\CommandLineTools\Java\jdk-17\bin\jpackage.exe --type app-image -n JavaFXSample --java-options "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=com.jfoenix" -m "sample/com.icuxika.MainApp" --icon "./src/main/resources/application.ico" --runtime-image "./target/build-link/" --dest "./target/build-package"
```

> 构建出安装包

```
C:\CommandLineTools\Java\jdk-17\bin\jpackage.exe -n JavaFXSample --app-image ./target/build-package/JavaFXSample --app-version 1.0.0 --dest ./target/build-link-package --temp ./target/build-link-package/temp --win-dir-chooser --win-menu --win-menu-group JavaFXSample --win-shortcut
```

### 方式五（GraalVM）

> - [下载](https://www.graalvm.org/downloads/)
> - 配置环境变量 `export GRAALVM_HOME=/Users/icuxika/CommandLineTools/graalvm-ee-java17-21.3.0/Contents/Home`
> - 执行 `$GRAALVM_HOME/bin/gu --jvm install native-image`

- ```mvn gluonfx:build```
- ```gradle nativeBuild```

> 当前构建即将完成时，会遇到`ld: library not found for -lsunec`错误，可以通过提示的错误日志位置，从日志中提取出对应的`gcc`命令，从中删除`-lsunec`部分并手动执行。
>
> 然后使用`mvn gluonfx:run`或`gradle nativeRun`来执行
>
> `target/gluonfx/x86_64-darwin/gvm/log` 或 `target/gluonfx/x86_64-darwin/gvm/log` 目录下的某个日志文件中
>
> 对应部分日志示例如下

```shell
PB Command for link: gcc /Users/icuxika/IdeaProjects/JavaFX-Package-Sample/target/gluonfx/x86_64-darwin/gvm/JavaFX-Package-Sample/AppDelegate.o /Users/icuxika/IdeaProjects/JavaFX-Package-Sample/target/gluonfx/x86_64-darwin/gvm/JavaFX-Package-Sample/launcher.o /Users/icuxika/IdeaProjects/JavaFX-Package-Sample/target/gluonfx/x86_64-darwin/gvm/tmp/SVM-1734905334702/com.icuxika.mainapp.o -ljava -lnio -lzip -lnet -lprefs -lj2pkcs11 -lfdlibm -lsunec -lextnet -ljvm -llibchelper -ldarwin -lpthread -lz -ldl -lstdc++ -mmacosx-version-min=10.12 -lobjc -lWebCore -lXMLJava -lJavaScriptCore -lbmalloc -licui18n -lSqliteJava -lXSLTJava -lPAL -lWebCoreTestSupport -lWTF -licuuc -licudata -Wl,-framework,Foundation -Wl,-framework,AppKit -Wl,-framework,ApplicationServices -Wl,-framework,OpenGL -Wl,-framework,QuartzCore -Wl,-framework,Security -Wl,-framework,Accelerate -Wl,-force_load,/Users/icuxika/.gluon/substrate/javafxStaticSdk/18-ea+2/darwin-x86_64/sdk/lib/libglass.a -Wl,-force_load,/Users/icuxika/.gluon/substrate/javafxStaticSdk/18-ea+2/darwin-x86_64/sdk/lib/libjavafx_font.a -Wl,-force_load,/Users/icuxika/.gluon/substrate/javafxStaticSdk/18-ea+2/darwin-x86_64/sdk/lib/libjavafx_iio.a -Wl,-force_load,/Users/icuxika/.gluon/substrate/javafxStaticSdk/18-ea+2/darwin-x86_64/sdk/lib/libprism_es2.a -Wl,-force_load,/Users/icuxika/.gluon/substrate/javafxStaticSdk/18-ea+2/darwin-x86_64/sdk/lib/libjfxwebkit.a -o /Users/icuxika/IdeaProjects/JavaFX-Package-Sample/target/gluonfx/x86_64-darwin/JavaFX-Package-Sample -L/Users/icuxika/.gluon/substrate/javafxStaticSdk/18-ea+2/darwin-x86_64/sdk/lib -L/Users/icuxika/CommandLineTools/graalvm-ee-java17-21.3.0/Contents/Home/lib/svm/clibraries/darwin-amd64 -L/Users/icuxika/CommandLineTools/graalvm-ee-java17-21.3.0/Contents/Home/lib/static/darwin-amd64
```
