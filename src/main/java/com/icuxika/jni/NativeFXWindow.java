package com.icuxika.jni;

import com.icuxika.MainApp;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

/**
 * mvn -Pjni clean compile
 */
public class NativeFXWindow {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeFXWindow.class);

    private static final String LIB_NAME = "NativeFXWindow.dll";

    private long hWnd;

    private Consumer<String> prevInstanceCallConsumer;

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

    public String getWindowName() {
        return getWindowName(hWnd);
    }

    public String getClassName() {
        return getClassName(hWnd);
    }

    public boolean registerHotKey(int id, int fsModifiers, int vk) {
        return registerHotKey(hWnd, id, fsModifiers, vk);
    }

    public boolean unregisterHotKey(int id) {
        return unregisterHotKey(hWnd, id);
    }

    public void setWindowTransparency() {
        setWindowTransparency(hWnd);
    }

    public void setWindowTransparencyAlpha(int alpha) {
        setWindowTransparencyAlpha(hWnd, alpha);
    }

    public void unsetWindowTransparency() {
        unsetWindowTransparency(hWnd);
    }

    public void initialize() {
        initialize(hWnd);
    }

    // ------------------------------------------------------------

    public void setPrevInstanceCallConsumer(Consumer<String> prevInstanceCallConsumer) {
        this.prevInstanceCallConsumer = prevInstanceCallConsumer;
    }

    // ------------------------------------------------------------
    public void callbackHotKey(int id) {
        LOGGER.info("callbackHotKey {}", id);
    }

    public void callPrevInstance(String message) {
        LOGGER.info("callPrevInstance {}", message);
        if (prevInstanceCallConsumer != null) {
            prevInstanceCallConsumer.accept(message);
        }
    }

    // ------------------------------------------------------------

    private static native long getHWnd(Stage stage);

    private static native String getWindowName(long hWnd);

    private static native String getClassName(long hWnd);

    private static native boolean registerHotKey(long hWnd, int id, int fsModifiers, int vk);

    private static native boolean unregisterHotKey(long hWnd, int id);

    private static native void setWindowTransparency(long hWnd);

    private static native void setWindowTransparencyAlpha(long hWnd, int alpha);

    private static native void unsetWindowTransparency(long hWnd);

    private native void initialize(long hWnd);

    public static native boolean isApplicationRunning(String mutexName);

    public static native void callPrevInstance(String message, String className, String windowName);
}
