package com.icuxika;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;

public class FXUtil {

    public static void runInFX(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }
        Platform.runLater(runnable);
    }

    public static void runInFXAndWait(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
            return;
        }
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                runnable.run();
            } finally {
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
