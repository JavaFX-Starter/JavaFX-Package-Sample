# JavaFX Package Sample

[English](#javafx-package-sample-en) | [简体中文](#javafx-package-sample-zh)

---

<div id="javafx-package-sample-en"></div>

## JavaFX Package Sample (English)

[![Windows Build](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/windows.yml/badge.svg)](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/windows.yml)
[![Ubuntu Build](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/ubuntu.yml/badge.svg)](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/ubuntu.yml)
[![macOS Build](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/macos.yml/badge.svg)](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/macos.yml)

This project is a comprehensive template for packaging and deploying modern JavaFX applications. It demonstrates a variety of advanced features, including native packaging, custom auto-updates, and native code integration (JNI).

### ✨ Features

- **Modern Tech Stack**: Built with Java 25+ and JavaFX 25+.
- **Multiple Packaging Options**:
    - Standard runnable JAR.
    - Self-contained native application using `jpackage`.
    - User-friendly Windows installer using NSIS.
- **Auto-Update Functionality**: A complete, custom-built auto-update mechanism with a C++ helper for robust, out-of-process updates.
- **Native Integration**: Demonstrates Java Native Interface (JNI) for calling native C++ code from JavaFX.
- **Modular & Non-Modular Builds**: Provides build configurations for both modular and non-modular applications, with discussions on the trade-offs.
- **Cross-Platform CI**: Pre-configured GitHub Actions for building and testing on Windows, macOS, and Ubuntu.
- **Internationalization (i18n)**: Supports English and Simplified Chinese.

### 🛠️ Prerequisites

Before you begin, ensure you have the following installed:
- **JDK 25 or higher**
- **Apache Maven**
- **C++ Build Environment** (e.g., Visual Studio on Windows) for building the native components.
- **NSIS** (Nullsoft Scriptable Install System) for creating the Windows installer.

### 🚀 Build Instructions

#### 1. Build a Runnable JAR

This is the simplest way to build the application.

```powershell
mvn clean package
java -jar ./target/jars/JavaFX-Package-Sample-1.0.3.jar
```
*Note: To prevent garbled text in the console, you might need to specify the encoding:*
```powershell
java -D"sun.stdout.encoding"=UTF-8 -D"sun.stderr.encoding"=UTF-8 -jar ./target/jars/JavaFX-Package-Sample-1.0.3.jar
```

#### 2. Build the Native Application Image

This packages the application into a self-contained directory with a native launcher and the required Java runtime.

```powershell
# For Windows (using the default 'win' profile)
mvn -Pwin exec:exec@image
```
The output will be in the `target/buildImage` directory.

#### 3. Build the Windows Installer

This creates a user-friendly `.exe` installer.

```powershell
# 1. First, build the native application image as shown above
mvn -Pwin clean package exec:exec@image

# 2. Then, run the PowerShell script to build the installer
./BuildInstaller.ps1
```
The installer will be created in the `nsis/out` directory.

#### 4. Build the Native Components (JNI)

If you modify the native C++ code, you need to rebuild the DLL/shared library.

```powershell
# 1. (If needed) Generate JNI headers from Java code
mvn -Pjni clean compile

# 2. Build the native library using the provided script
./src/native/build.ps1
```

#### 5. Modular Build

To create a smaller application package, you can use the modular build profile. Note that the current auto-update logic is not optimized for the single large `modules` file produced by this build.

```powershell
# This script handles the modular build process
./BuildModular.ps1
```

### ⚙️ How the Auto-Update Works

The auto-update feature allows the application to update itself without requiring the user to download a new installer.

1.  **Check for Updates**: The app fetches a `latest.json` file from a server to see if a new version is available.
2.  **Compare Files**: If a new version exists, it downloads an `update-index.json` file which contains checksums for all application files. It compares this with a locally generated index to determine which files need to be added, updated, or deleted.
3.  **Download Files**: The application downloads all necessary new/updated files to a temporary directory.
4.  **Hand-off to Helper**: The JavaFX app closes itself and launches the native C++ helper (`auto-update-helper.exe`).
5.  **Apply Updates**: The C++ helper, now running independently, copies the downloaded files into the application directory, replacing or deleting old ones.
6.  **Relaunch**: Once the update is complete, the helper relaunches the main application and then exits.

This out-of-process approach ensures that no application files are locked during the update process.

---

<div id="javafx-package-sample-zh"></div>

## JavaFX Package Sample (简体中文)

[![Windows Build](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/windows.yml/badge.svg)](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/windows.yml)
[![Ubuntu Build](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/ubuntu.yml/badge.svg)](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/ubuntu.yml)
[![macOS Build](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/macos.yml/badge.svg)](https://github.com/icuxika/JavaFX-Package-Sample/actions/workflows/macos.yml)

本项目是一个用于打包和部署现代 JavaFX 应用程序的综合性模板。它演示了多种高级功能，包括原生打包、自定义自动更新机制和原生代码集成（JNI）。

### ✨ 功能特性

- **现代技术栈**: 基于 Java 25+ 和 JavaFX 25+ 构建。
- **多种打包选项**:
    - 标准的可执行 JAR。
    - 使用 `jpackage` 构建的自包含原生应用程序。
    - 使用 NSIS 创建的用户友好的 Windows 安装包。
- **自动更新功能**: 一套完整的自定义自动更新机制，配备一个 C++ 辅助程序以实现稳健的进程外更新。
- **原生代码集成**: 演示了如何通过 Java 原生接口 (JNI) 从 JavaFX 调用原生 C++ 代码。
- **模块化与非模块化构建**: 同时提供了模块化和非模块化应用的构建配置，并探讨了其优缺点。
- **跨平台持续集成**: 已预先配置好 GitHub Actions，可在 Windows、macOS 和 Ubuntu 上进行构建和测试。
- **国际化 (i18n)**: 支持英文和简体中文。

### 🛠️ 环境要求

在开始之前，请确保您已安装以下软件：
- **JDK 25 或更高版本**
- **Apache Maven**
- **C++ 构建环境** (例如 Windows 上的 Visual Studio)，用于构建原生组件。
- **NSIS** (Nullsoft 脚本安装系统)，用于创建 Windows 安装包。

### 🚀 构建指南

#### 1. 构建可执行 JAR

这是构建应用程序最简单的方式。

```powershell
mvn clean package
java -jar ./target/jars/JavaFX-Package-Sample-1.0.3.jar
```
*注意：为防止控制台输出乱码，您可能需要指定编码：*
```powershell
java -D"sun.stdout.encoding"=UTF-8 -D"sun.stderr.encoding"=UTF-8 -jar ./target/jars/JavaFX-Package-Sample-1.0.3.jar
```

#### 2. 构建原生应用程序镜像

这将把应用程序打包成一个自包含的目录，其中包含原生启动器和所需的 Java 运行时。

```powershell
# 适用于 Windows (使用默认的 'win' profile)
mvn -Pwin exec:exec@image
```
构建产物将位于 `target/buildImage` 目录中。

#### 3. 构建 Windows 安装包

这将创建一个用户友好的 `.exe` 安装程序。

```powershell
# 1. 首先，如上所示构建原生应用程序镜像
mvn -Pwin clean package exec:exec@image

# 2. 然后，运行 PowerShell 脚本来构建安装包
./BuildInstaller.ps1
```
安装包将创建在 `nsis/out` 目录中。

#### 4. 构建原生组件 (JNI)

如果您修改了原生 C++ 代码，则需要重新构建 DLL/共享库。

```powershell
# 1. (如果需要) 从 Java 代码生成 JNI 头文件
mvn -Pjni clean compile

# 2. 使用提供的脚本构建原生库
./src/native/build.ps1
```

#### 5. 模块化构建

若要创建一个体积更小的应用程序包，您可以使用模块化构建配置。请注意，当前的自动更新逻辑并未针对模块化构建产生的单个巨大的 `modules` 文件进行优化。

```powershell
# 此脚本将处理模块化构建过程
./BuildModular.ps1
```

### ⚙️ 自动更新工作原理

自动更新功能允许应用程序自我更新，而无需用户手动下载新的安装包。

1.  **检查更新**: 应用程序从服务器获取一个 `latest.json` 文件，以判断是否有新版本可用。
2.  **文件比对**: 如果存在新版本，它会下载一个 `update-index.json` 文件，其中包含所有应用文件的校验和。通过与本地生成的索引进行比对，确定需要新增、更新或删除哪些文件。
3.  **下载文件**: 应用程序将所有需要的新文件/更新文件下载到一个临时目录。
4.  **切换至辅助程序**: JavaFX 应用自行关闭，并启动原生的 C++ 辅助程序 (`auto-update-helper.exe`)。
5.  **应用更新**: C++ 辅助程序独立运行，将下载的文件复制到应用程序目录中，替换或删除旧文件。
6.  **重新启动**: 更新完成后，辅助程序会重新启动主应用程序，然后退出。

这种进程外更新的方式确保了在更新过程中没有任何应用程序文件被锁定。
