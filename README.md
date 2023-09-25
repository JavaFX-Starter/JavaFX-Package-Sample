JavaFX 打包示例
------------------------------------------------------------

## 说明
- 当前为Java21版本，其他版本请切换分之查看
- 由于本项目中gradle配置使用的插件功能更完整，所以推荐使用gradle来构建本项目，如果只能选择maven的话，由于`Maven plugin for JavaFX`已经许久没有更新，`JDK21`中`jlink`的参数`--compress`新的值也不支持，建议去寻找第三方维护的插件来使用，当然你的项目足够简单的话，`pom.xml`自定义的构建命令也许能够完全支持你的项目
- 若你实在没有办法处理非模块化的第三方依赖及分裂包相关的模块化问题，可以参考使用[The Badass Runtime Plugin](https://badass-runtime-plugin.beryx.org/releases/latest/) 插件来对非模块化项目进行构建
  - [IMFrameworkFX](https://github.com/icuxika/IMFrameworkFX) （Java）
  - [KtFX-Lets-Plot](https://github.com/icuxika/KtFX-Lets-Plot/tree/non-modular) 的`non-modular`分支实现 （Kotlin）

## 环境设置
#### `jpackage` 构建出安装包依赖的程序（如果只需要构建出可执行程序就可以跳过此配置）
- Windows: 需要安装 [WIX TOOLSET](https://wixtoolset.org/) ，并将其bin目录添加到环境变量中
- macOS/Linux: 缺失的包一般可以根据提示通过包管理器直接安装
#### 在IDEA中构建项目
- 项目结构 SDK 选择 JDK21
- Gradle：Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> 指定 Gradle JVM 为项目 SDK
#### 使用命令行（此处演示使用Windows的PowerShell，macOS和Linux假设你熟悉在命令行中设置临时环境变量）
> // `$env:xxx = "yyyy"`是设置一个临时环境变量，重启终端或者开启一些新的PowerShell标签页会失效
- 设置`JAVA_HOME`环境变量 `$env:JAVA_HOME = "C:\CommandLineTools\Java\jdk-21\"`
  - > 使用 `gradlew`或者`mvn`的时候，只需要存在`JAVA_HOME`这个环境变量就可以，不需要`java`等命令配置在环境变量PATH中
- 设置`GRAALVM_HOME`环境变量 `$env:GRAALVM_HOME = "C:\CommandLineTools\Java\graalvm-jdk-21+35.1"`
#### 对cmd的补充
使用`set JAVA_HOME=C:\CommandLineTools\Java\jdk-21`方式来设定临时环境变量

## 项目构建
### `jpackage`
##### gradle
> 构建可执行程序
```shell
.\gradlew.bat jpackageImage
```
> 构建安装包
```shell
.\gradlew.bat jpackage
```
##### maven
请根据不同操作系统修改`exec-maven-plugin`插件中有关下方两个命令的`--icon`的参数值，Windows为`${project.basedir}/src/main/resources/application.ico`，macOS为`${project.basedir}/src/main/resources/application.icns`，Linux为`${project.basedir}/src/main/resources/application.png`
> 运行（Maven plugin for JavaFX）
```shell
mvn javafx:run
```
> 构建出`.bat`形式的可执行程序（Maven plugin for JavaFX）
```shell
mvn javafx:jlink
```
> 构建可执行程序（手动拼接jpackage命令），`此命令构建出的结果其实与上面差别不大，但是是.exe的形式，并且图标已经设置好了`
```shell
mvn clean
mvn package
mvn exec:exec@image
```
> 构建安装包（手动拼接jpackage命令）
```shell
mvn clean
mvn package
mvn exec:exec@installer
```

### `gluon`
> `GluonFX plugin for Maven`和`GluonFX plugin for Gradle`依赖的`Gluon Substrate`存在一个bug，暂时无法在`GraalVM 21`上运行，不过你可以
- 下载[Oracle GraalVM](https://www.graalvm.org/downloads/)
- 请运行在由`Visual Studio`提供的命令行环境中，`Windows 终端`可以直接在标签页中打开`Developer PowerShell for VS 2022`，其他用户可以在开始菜单的`Visual Studio 2022`文件夹中找到
##### gradle
###### 构建与运行
```shell
.\gradlew.bat nativeBuild
.\gradlew.bat nativeRun
```
###### `Downloading JavaFX static libs...`网络问题
Gluon下载`JavaFX static libs`涉及到的相关源码[FileOps.java](https://github.com/gluonhq/substrate/blob/master/src/main/java/com/gluonhq/substrate/util/FileOps.java)，因此可以通过配置`java`的一些系统属性（需要梯子）来加速，在项目根目录下新建文件`gradle.properties`内容如下，配置项的有关文档[Java Networking and Proxies](https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html)，gradle相关文档[Accessing the web through a proxy](https://docs.gradle.org/current/userguide/build_environment.html#ex-configuring-an-http-proxy-using-gradle-properties)
```properties
org.gradle.jvmargs=-Dfile.encoding=UTF-8
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
```
重新执行相关命令，可能会因为之前的下载缓存导致重新执行命令失败，这时可以去删除此文件夹`C:\Users\你的用户名\.gluon\substrate`再重新运行，`JavaFX static libs`下载成功后可以注释掉相关代理配置以防其他平台梯子配置不同
##### maven
###### 构建与运行
```shell
mvn gluonfx:build
mvn gluonfx:run
```

###### `Downloading JavaFX static libs...`网络问题
> gradle 也可以在不配置`gradle.properties`的情况下通过`.\gradlew.bat nativeBuild -D"https.proxyHost"=127.0.0.1 -D"https.proxyPort"=7890`来设置相关属性
```shell
mvn gluonfx:build -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890
```

## 补充
有时候`mvn gluonfx:build`或者`.\gradlew.bat jpackageImage`发生了错误后，执行`.\gradlew.bat clean`提示文件被占用，这时候可以打开`任务管理器-性能-资源监视器-CPU`，在`关联的句柄`处输入`gluon`、`jpackage`等关键词来检索出卡住的相关进程并干掉他
