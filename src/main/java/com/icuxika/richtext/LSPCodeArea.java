package com.icuxika.richtext;

import com.icuxika.lsp.DiagnosticMessage;
import com.icuxika.lsp.LSPAgent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;

import java.util.List;

public class LSPCodeArea extends CodeArea {

    private final TextMateSyntaxDecorator syntaxDecorator;
    private LSPAgent lspAgent;
    private volatile boolean shouldListen = true;
    private int lastChangeIndex = 0;
    private Timeline debounceTimeline;

    public LSPCodeArea(boolean isLight, String text) {
        setLineNumbersEnabled(true);
        setContentPadding(new Insets(4));
        setBorder(new Border(new BorderStroke(Color.DODGERBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(2))));
        // https://github.com/microsoft/vscode/tree/main/extensions/java/syntaxes
        // https://github.com/microsoft/vscode/tree/main/extensions/theme-defaults/themes
        syntaxDecorator = new TextMateSyntaxDecorator(
                this,
                "/richtext/syntaxes/java.tmLanguage.json",
                isLight ? "/richtext/themes/light_vs.json" : "/richtext/themes/dark_vs.json"
        );
        setSyntaxDecorator(syntaxDecorator);
        setText(text);
    }

    public void startLanguageServer() {
        lspAgent = new LSPAgent();
        lspAgent.setDiagnosticMessageConsumer(diagnosticMessage -> {
            shouldListen = false;
            applyChange(diagnosticMessage);
            shouldListen = true;
        });
        Thread thread = new Thread(() -> {
            lspAgent.initialize();
            lspAgent.sendOpenTextDocument(getText());
            getModel().addListener(_ -> {
                if (!shouldListen) {
                    return;
                }

                if (debounceTimeline != null) {
                    debounceTimeline.stop();
                }

                debounceTimeline = new Timeline(new KeyFrame(
                        Duration.millis(300),
                        _ -> {
                            if (syntaxDecorator != null) {
                                syntaxDecorator.updateDiagnosticMessage(List.of());
                                lspAgent.sendChangeTextDocument(getText());
                            }
                        }
                ));

                debounceTimeline.setCycleCount(1);
                debounceTimeline.play();
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stopLanguageServer() {
        lspAgent.shutdown();
    }

    /**
     * 应用诊断消息到代码区域
     */
    public void applyChange(DiagnosticMessage diagnosticMessage) {
        syntaxDecorator.updateDiagnosticMessage(List.of(diagnosticMessage));
        applyChange(diagnosticMessage.startLine());
        lastChangeIndex = diagnosticMessage.startLine();
    }

    public void clearLastChange() {
        syntaxDecorator.updateDiagnosticMessage(List.of());
        applyChange(lastChangeIndex);
    }

    private void applyChange(int index) {
        int length = getModel().getPlainText(index).length();
        ((CodeTextModel) getModel()).insertText(TextPos.ofLeading(index, length), " ");
        replaceText(TextPos.ofLeading(index, length), TextPos.ofLeading(index, length + 1), "", false);
    }
}
