package com.icuxika.jni;

/**
 * mvn exec:exec@jni-generate
 * javac -J-D"sun.stdout.encoding"=UTF-8 -J-D"sun.stderr.encoding"=UTF-8 -h .\src\native\include\ .\src\main\java\com\icuxika\jni\NativeFXWindow.java
 */
public class NativeFXWindow {
    public static native long getHWnd(Object stage);

    public static native String getWindowText(long hWnd);

    public static native String getClassName(long hWnd);
}
