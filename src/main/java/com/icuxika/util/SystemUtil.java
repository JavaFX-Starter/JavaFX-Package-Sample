package com.icuxika.util;

import com.icuxika.AppUpdateTool;
import com.icuxika.constant.SystemConstant;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SystemUtil {

    public static boolean isSystemPath(Path path) {
        return path.toAbsolutePath().startsWith(Path.of(System.getenv("ProgramFiles")));
    }

    /**
     * 获取当前应用的可执行文件路径，基于 jpackage 生成的应用程序映像
     */
    public static Path getExePath() {
        try {
            Path target;
            Path jarPath = Path.of(AppUpdateTool.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jarPath.toString().contains("classes")) {
                // 开发路径，应用程序映像已经生成，但依旧在 idea 中运行源码
                target = Path.of(jarPath.toFile().getParent()).resolve("buildImage").resolve(SystemConstant.APP_IMAGE_NAME);
            } else {
                // 执行应用程序映像中的程序
                target = Path.of(jarPath.toFile().getParentFile().getParent());
            }
            return target;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getAutoUpdateHelperExePath() {
        return getExePath().resolve("auto-update-helper.exe");
    }

    public static Path getLocalAppData() {
        return Paths.get(System.getenv("LOCALAPPDATA"), SystemConstant.LOCAL_APP_DATA_KEY);
    }

    public static Path getLocalAppDataUpdate() {
        return getLocalAppData().resolve("update");
    }

}
