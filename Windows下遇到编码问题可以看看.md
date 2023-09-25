Windows下一些编码问题的记录
==========

## 添加代码
在`MainApp`的`start`方法末尾添加
```
System.out.println("你好");
```

## PowerShell
查看当前编码
```shell
[System.Console]::OutputEncoding
```
```shell
BodyName          : gb2312
EncodingName      : 简体中文(GB2312)
HeaderName        : gb2312
WebName           : gb2312
WindowsCodePage   : 936
IsBrowserDisplay  : True
IsBrowserSave     : True
IsMailNewsDisplay : True
IsMailNewsSave    : True
IsSingleByte      : False
EncoderFallback   : System.Text.InternalEncoderBestFitFallback
DecoderFallback   : System.Text.InternalDecoderBestFitFallback
IsReadOnly        : False
CodePage          : 936
```

### 1.使用`mvn javafx:run`
> Java标准输出流(GB2312) -> PowerShell(GB2312)
```shell
你好
```

### 2.使用`.\gradlew.bat run`
> Java标准输出流(GB2312) -> gradle(UTF-8) -> PowerShell(GB2312)
```shell
锟斤拷锟?
```
### 3.修改`build.gradle`中的`application`配置块为
> 使标准输出流使用`UTF-8`编码
```shell
application {
    applicationName = "JavaFXSample"
    mainModule.set("sample")
    mainClass.set("com.icuxika.MainApp")
    applicationDefaultJvmArgs = [
            // ZGC
            "-XX:+UseZGC",
            // 当遇到空指针异常时显示更详细的信息
            "-XX:+ShowCodeDetailsInExceptionMessages",
            "-Dsun.java2d.opengl=true",
            // 不添加此参数，打包成exe后，https协议的网络图片资源无法加载
            "-Dhttps.protocols=TLSv1.1,TLSv1.2",
            "-Dsun.stdout.encoding=UTF-8",
            "-Dsun.stderr.encoding=UTF-8"
    ]
}
```
### 4.再次执行`.\gradlew.bat run`
> Java标准输出流(UTF-8) -> gradle(UTF-8) -> PowerShell(GB2312)
```shell
浣犲ソ
```

### 5.设置`PowerShell`使用`UTF-8`编码
```shell
[System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```
```shell
Preamble          :
BodyName          : utf-8
EncodingName      : Unicode (UTF-8)
HeaderName        : utf-8
WebName           : utf-8
WindowsCodePage   : 1200
IsBrowserDisplay  : True
IsBrowserSave     : True
IsMailNewsDisplay : True
IsMailNewsSave    : True
IsSingleByte      : False
EncoderFallback   : System.Text.EncoderReplacementFallback
DecoderFallback   : System.Text.DecoderReplacementFallback
IsReadOnly        : False
CodePage          : 65001
```

### 6.使用`mvn javafx:run`
> Java标准输出流(GB2312) -> PowerShell(UTF-8)
```shell
���
```

### 7.使用`.\gradlew.bat run`
> Java标准输出流(UTF-8) -> PowerShell(UTF-8)
```shell
你好
```

### 8.修改`build.gradle`中的`application`配置块为
```shell
application {
    applicationName = "JavaFXSample"
    mainModule.set("sample")
    mainClass.set("com.icuxika.MainApp")
    applicationDefaultJvmArgs = [
            // ZGC
            "-XX:+UseZGC",
            // 当遇到空指针异常时显示更详细的信息
            "-XX:+ShowCodeDetailsInExceptionMessages",
            "-Dsun.java2d.opengl=true",
            // 不添加此参数，打包成exe后，https协议的网络图片资源无法加载
            "-Dhttps.protocols=TLSv1.1,TLSv1.2"
    ]
}
```

### 9.再次执行`.\gradlew.bat run`
> Java标准输出流(GB2312) -> gradle(UTF-8) -> PowerShell(UTF-8)
> 
```shell
���
```

### 总结
- java 默认标准输出流使用的编码由操作系统的编码决定
- maven 以`Maven plugin for JavaFX`插件为例，它实际获取到`java`和`jlink`的路径后通过`cmd /c java`的方式来执行命令，且插件没有提供参数让你传递`vm`选项，因此无法传递`sun.stdout.encoding`之类的属性给到插件里的命令，想要不乱码，只能使显示输出的前端编码匹配操作系统的默认编码。注意：其他的maven项目执行代码的`java`程序如果没有隔了这么一层，通过对`maven-compiler-plugin`设置`<compilerArguments>`来设置相关选项
- gradle 由于`gradle run`没有像`mvn javafx:run`这样隔了一层间接调用`java`可以通过`applicationDefaultJvmArgs`来设置选项。注意：`gradle jpackageImage`的情况就跟上面很像了
- gradle 还有一处特别，gradle编译项目时，相关流会经历 gradle -> gradle daemon -> java compiler，java compiler -> gradle daemon -> gradle，其中 `gradle daemon -> gradle` 的时候，默认使用了`UTF-8`来进行序列化，所以gradle相关的命令注释多了中间一层，同时由于中间默认多了层`UTF-8`转化，且插件无法传入`vm`选项时，这个乱码无解，只能Windows操作系统全局设置`UTF-8`
- 上面所说的`隔了一层`主要意思是指能不能传递`vm`选项给到`java`或`jlink`

### 补充
如果你在`PowerShell`中执行`[System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8`后，发现`java --help`乱码了，你可以执行`[System.Console]::OutputEncoding = [System.Text.Encoding]::Default`回到默认设置，也可以
```shell
C:\CommandLineTools\Java\jdk-21\bin\java.exe -D"sun.stdout.encoding"=UTF-8 -D"sun.stderr.encoding"=UTF-8 --help
C:\CommandLineTools\Java\jdk-21\bin\jpackage.exe --help -J-D"sun.stdout.encoding"=UTF-8 -J-D"sun.stderr.encoding"=UTF-8
```
> 注意目标为`java`时，`-D`要在`--help`的前面，目标为`jpackage`时，`-J-D`可以放在后面