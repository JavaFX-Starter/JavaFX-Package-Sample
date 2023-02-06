JavaFX 打包示例（GraalVM 还没测试）
------------------------------------------------------------

### 当前为Java19版本，其他版本请切换分之查看

### 一些变动
- 移除了`JFoenix`（已不再更新，在新版本jdk中，许多类已不兼容模块化相关变更），使用[MaterialFX](https://github.com/palexdev/MaterialFX) 作为示例使用的控件库
- 因为移除了`JFoenix`，所以项目也移除了有关`--add-exports`或是`--add-opens`的配置，如果后续使用第三方库或者`jdk`本身内部库的时候，遇到了相关错误，可以查看17及之前的版本的配置是怎么配置的
- 添加了有关`i18n`的配置，支持各控件`StringProperty`进行属性绑定使用，并通过`AppResource.setLanguage(Locale.SIMPLIFIED_CHINESE);` 来进行语言动态切换
- 有关客户端其他常用封装，可以参考[IMFrameworkFX](https://github.com/icuxika/IMFrameworkFX)
  - 如何封装一个Http请求工具类，可以查看`com.icuxika.api.API`类，支持对类似`Spring Boot`后端返回的`json`数据自动泛型转换对应实体类，并且提供了文件下载的进度显示，以及请求上传的进度显示的支持
  - 通过注解来使用`FXML`，并支持一个属性变更动态切换不同主题`css`样式文件，可以查看`com.icuxika.AppView`类

### 若你实在没有办法处理非模块化的第三方依赖及分裂包相关的模块化问题，可以参考使用[The Badass Runtime Plugin](https://badass-runtime-plugin.beryx.org/releases/latest/) 插件来对非模块化项目进行构建

- [IMFrameworkFX](https://github.com/icuxika/IMFrameworkFX) （Java）
- [KtFX-Lets-Plot](https://github.com/icuxika/KtFX-Lets-Plot/tree/non-modular) 的`non-modular`分支实现 （Kotlin）

### 准备工作

- 命令行环境：```set JAVA_HOME="jdk19主目录"```，PowerShell中设置的临时环境变量对mvn无效（不是很确定，但你要考虑这一因素），在cmd中设置的有效
- IDEA：
  - Gradle：Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> 指定Gradle JVM为Java19
- Windows：需要安装 [WIX TOOLSET](https://wixtoolset.org/) ，并将其bin目录添加到环境变量中
- 其他平台缺失的包一般可以根据提示通过包管理器直接安装

### 方式一（Gradle Task）

> 当前配置下，此方式（包括Gradle方式的手动版）存在问题，原因是，使用`com.gluonhq.gluonfx-gradle-plugin`插件需要依赖`org.openjfx.javafxplugin`插件，这种情况会产生两个依赖，
> 如`javafx-base`与`javafx:base:mac`，然后Java默认的模块化系统读取的时候就会从两个包中发现两个一样的模块从而产生错误。解决方式：一、方式二不受此问题影响；二：不需要使用GraalVm的情况下，注释掉
> `com.gluonhq.gluonfx-gradle-plugin`与`org.openjfx.javafxplugin`插件，然后注释掉对应的`javafx`依赖配置块与`gluonfx`配置块，最后手动在`dependencies`中设置`JavaFX`的依赖。

使用gradle自定义任务拼接命令进行打包（不要变动项目的gradle版本设置，同时请确认gradle运行在java18环境下）

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

- 重新构建请先执行`mvn clean`
- 构建`jar`包（本步骤会自动拷贝项目所需第三方依赖到`target/alternateLocation`目录下） -> ```mvn package```
- 构建镜像 -> ```mvn exec:exec@image```
- 构建安装包 -> ```mvn exec:exec@installer```

### 方式四（手动构建，为方式一的手动版）（待更新，请先查看<a href="Windows下手动构建的演示.md">Windows下手动构建的演示</a>）
> `当前版本待测试`

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
> `Windows版本待测试`
#### 说明
- [下载](https://www.graalvm.org/downloads/)
- 配置环境变量 `export GRAALVM_HOME=export GRAALVM_HOME=/Users/icuxika/CommandLineTools/graalvm-ce-java17-22.0.0.2/Contents/Home`
- （`22.0.0.2` 没有此操作也未影响后面的步骤）执行 `$GRAALVM_HOME/bin/gu --jvm install native-image`
- `GraalVM Community 22.0`目前并不支持在`java18`的环境下进行运行，因此`<maven.compiler.release>17</maven.compiler.release>`项目依旧需要使用`java17`，如果你不使用`GraalVM`，则可以更改为`18`
- 初次执行，会有`Downloading JavaFX static libs...`的过程，此下载可能需要翻墙
- 除了`GRAALVM_HOME`的配置，你应当确保`JAVA_HOME`也正确配置
- 建议在命令行操作，方便测试不同环境变量下的效果

##### 构建
- ```mvn gluonfx:build```
- ```gradle nativeBuild```

##### 运行
- ```mvn gluonfx:run```
- ```gradle nativeRun```

##### 小问题
> macOS上`mvn gluonfx:run`或`gradle nativeRun`不会报告任何错误，但直接双击生成的程序运行时，关联的控制台窗口会报告
> ```java.lang.NoSuchMethodError: com.sun.glass.ui.mac.MacAccessible.accessibilityAttributeNames```，但不会影响程序的运行

