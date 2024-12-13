package com.icuxika.jni;

import com.icuxika.MainApp;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeFXWindowWrapper {

    private static final String LIB_NAME = "NativeFXWindow.dll";

    private final long hWnd;

    public NativeFXWindowWrapper(Stage stage) {
        hWnd = NativeFXWindow.getHWnd(stage);
    }

    static {
        try (InputStream inputStream = MainApp.class.getResourceAsStream("/native/lib/" + LIB_NAME)) {
            if (inputStream != null) {
                System.out.println(inputStream.available());
                Path tempFilePath = Files.createTempFile(LIB_NAME, "");
                Files.copy(inputStream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
                System.load(tempFilePath.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
}
