package com.icuxika.util;

import java.nio.file.Path;

public class SystemUtil {

    public static boolean isSystemPath(Path path) {
        return path.toAbsolutePath().startsWith(Path.of(System.getenv("ProgramFiles")));
    }

}
