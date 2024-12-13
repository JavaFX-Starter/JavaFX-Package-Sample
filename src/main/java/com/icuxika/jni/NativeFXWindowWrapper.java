package com.icuxika.jni;

import com.icuxika.MainApp;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeFXWindowWrapper {

    private static final String LIB_NAME = "NativeFXWindow.dll";

    private long hWnd;

    public NativeFXWindowWrapper() {
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
        return NativeFXWindow.getWindowText(hWnd);
    }

    public String getClassName() {
        return NativeFXWindow.getClassName(hWnd);
    }

    public void setWindowTransparency() {
        NativeFXWindow.setWindowTransparency(hWnd);
    }

    public void unsetWindowTransparency() {
        NativeFXWindow.unsetWindowTransparency(hWnd);
    }
}
