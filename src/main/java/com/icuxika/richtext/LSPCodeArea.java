package com.icuxika.richtext;

import com.icuxika.AppResource;
import com.icuxika.FXUtil;
import com.icuxika.constant.Theme;
import com.icuxika.lsp.DiagnosticMessage;
import com.icuxika.lsp.LSPAgent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

public class LSPCodeArea extends CodeArea {

    private final TextMateSyntaxDecorator syntaxDecorator;
    private LSPAgent lspAgent;
    private volatile boolean shouldListen = true;
    private int lastChangeIndex = 0;
    private Timeline debounceTimeline;

    public LSPCodeArea(boolean isLight, String text) {
        setPrefHeight(240);
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

        AppResource.themeProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                if (newValue == Theme.LIGHT) {
                    syntaxDecorator.readTheme("/richtext/themes/light_vs.json");
                } else {
                    syntaxDecorator.readTheme("/richtext/themes/dark_vs.json");
                }
                syntaxDecorator.applyTheme();

                shouldListen = false;
                syntaxDecorator.refresh();
                shouldListen = true;
            }
        });
    }

    public void startLanguageServer() {
        lspAgent = new LSPAgent();
        lspAgent.setDiagnosticMessageConsumer(diagnosticMessage -> {
            shouldListen = false;
            applyChange(diagnosticMessage);
            shouldListen = true;
        });
        lspAgent.setCompletionConsumer(completionItems -> FXUtil.runInFX(() -> showCompletion(getCaretPosition(), completionItems)));
        Thread thread = new Thread(() -> {
            lspAgent.initialize();
            lspAgent.sendOpenTextDocument(getText());
            getModel().addListener(ch -> {
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

                                if (ch.getLinesAdded() == 0) {
                                    String plainText = getModel().getPlainText(ch.getStart().index());
                                    String nextChar = plainText.substring(ch.getStart().charIndex() + ch.getCharsAddedTop());
                                    if (nextChar.equals(".")) {
                                        lspAgent.completion(ch.getStart().index(), ch.getStart().charIndex() + ch.getCharsAddedTop() + 1);
                                    }
                                }
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
        if (lspAgent != null) {
            lspAgent.shutdown();
        }
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

    public void showCompletion(TextPos caretPosition, List<CompletionItem> completionItems) {
        Node node = lookup(".caret");
        if (node instanceof Path path) {
            Bounds bounds = path.localToScreen(path.getBoundsInLocal());
            ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().addAll(completionItems.stream().map(completionItem -> {
                MenuItem menuItem = new MenuItem();
                menuItem.setText(completionItem.getLabel());
                menuItem.setOnAction(_ -> {
                    ((CodeTextModel) getModel()).insertText(caretPosition, completionItem.getInsertText());
                    moveLineEnd();
                });
                return menuItem;
            }).toList());
            contextMenu.show(this, bounds.getMaxX(), bounds.getMaxY());
        }
    }
}
