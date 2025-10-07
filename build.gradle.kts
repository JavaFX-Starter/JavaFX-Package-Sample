import org.gradle.internal.os.OperatingSystem

plugins {
    application
    java
    id("org.beryx.jlink") version "3.1.4-rc"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gluonhq.gluonfx-gradle-plugin") version "1.0.27"
}

group = "com.icuxika"
version = "1.0.0"

repositories {
    mavenCentral()
}

application {
    applicationName = "JavaFXSample"
    mainModule.set("sample")
    mainClass.set("com.icuxika.MainApp")
    applicationDefaultJvmArgs = listOf(
        // ZGC
        "-XX:+UseZGC",
        // 当遇到空指针异常时显示更详细的信息
        "-XX:+ShowCodeDetailsInExceptionMessages",
        "-Dsun.java2d.opengl=true",
        // 不添加此参数，打包成exe后，https协议的网络图片资源无法加载
        "-Dhttps.protocols=TLSv1.1,TLSv1.2",
        "--enable-native-access=javafx.graphics"
    )
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.graphics", "javafx.fxml", "javafx.swing", "javafx.media", "javafx.web")
}

dependencies {
    implementation("io.github.palexdev:materialfx:11.16.1")
}

jlink {
    options.set(listOf("--strip-debug", "--compress", "zip-9", "--no-header-files", "--no-man-pages"))

    launcher {
        name = application.applicationName
        imageName.set(application.applicationName)
    }

    imageZip.set(project.file("${project.layout.buildDirectory.get()}/image-zip/JavaFXSample.zip"))

    jpackage {
        outputDir = "build-package"
        imageName = application.applicationName
        skipInstaller = false
        installerName = application.applicationName
        appVersion = version.toString()

        if (OperatingSystem.current().isWindows) {
            icon = "src/main/resources/application.ico"
            installerOptions = listOf(
                "--win-dir-chooser",
                "--win-menu",
                "--win-shortcut",
                "--win-menu-group",
                application.applicationName
            )
        }
        if (OperatingSystem.current().isMacOsX) {
            icon = "src/main/resources/application.icns"
        }
        if (OperatingSystem.current().isLinux) {
            icon = "src/main/resources/application.png"
            installerType = "deb"
            installerOptions = listOf(
                "--linux-deb-maintainer",
                "icuxika@outlook.com",
                "--linux-menu-group",
                application.applicationName,
                "--linux-shortcut"
            )
        }
    }
}

gluonfx {
    bundlesList = listOf(
        "LanguageResource",
        "LanguageResource_en",
        "LanguageResource_zh_CN"
    )
    if (OperatingSystem.current().isMacOsX) {
        compilerArgs = listOf("-Dsvm.platform=org.graalvm.nativeimage.Platform\$MACOS_AMD64")
    }
}