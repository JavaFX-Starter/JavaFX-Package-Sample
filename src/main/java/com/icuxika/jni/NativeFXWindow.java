package com.icuxika.jni;

import com.icuxika.MainApp;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * mvn -Pjni clean compile
 */
public class NativeFXWindow {

    private static final String LIB_NAME = "NativeFXWindow.dll";

    private long hWnd;

    public NativeFXWindow() {
    }

    static {
        try (InputStream inputStream = MainApp.class.getResourceAsStream("/native/lib/" + LIB_NAME)) {
            if (inputStream != null) {
                Path tempFilePath = Files.createTempFile(LIB_NAME, "");
                Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
                System.load(tempFilePath.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 需要在{@link Stage#show()}之后调用
     */
    public void initialize(Stage stage) {
        hWnd = NativeFXWindow.getHWnd(stage);
    }

    public long getHWnd() {
        return hWnd;
    }

    public String getWindowText() {
        return getWindowText(hWnd);
    }

    public String getClassName() {
        return getClassName(hWnd);
    }

    public void setWindowTransparency() {
        setWindowTransparency(hWnd);
    }

    public void unsetWindowTransparency() {
        unsetWindowTransparency(hWnd);
    }

    // ------------------------------------------------------------

    private static native long getHWnd(Stage stage);

    private static native String getWindowText(long hWnd);

    private static native String getClassName(long hWnd);

    public static native boolean registerHotKey(int id, int fsModifiers, int vk);

    public static native boolean unregisterHotKey(int id);

    private static native void setWindowTransparency(long hWnd);

    private static native void unsetWindowTransparency(long hWnd);
}
