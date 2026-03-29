package com.icuxika;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextFlow;

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

    public static void toggleStyleClass(Node node, String styleClass, boolean enabled) {
        if (enabled && !node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        } else if (!enabled) {
            node.getStyleClass().remove(styleClass);
        }
    }

    public static TextFlow createTextFlow(String svgContent, double size) {
        TextFlow textFlow = new TextFlow();
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(svgContent);
        textFlow.setShape(svgPath);
        textFlow.setMinWidth(size);
        textFlow.setMinHeight(size);
        textFlow.setMaxWidth(size);
        textFlow.setMaxHeight(size);
        return textFlow;
    }
}
