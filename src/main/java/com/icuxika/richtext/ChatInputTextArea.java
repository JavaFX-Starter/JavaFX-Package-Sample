package com.icuxika.richtext;

import com.icuxika.AppResource;
import com.icuxika.MainApp;
import com.icuxika.constant.Theme;
import javafx.scene.input.Clipboard;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.TextPos;

import java.util.List;

public class ChatInputTextArea extends RichTextArea {

    private final ReadWriteTextModel readWriteTextModel = new ReadWriteTextModel();

    public ChatInputTextArea() {
        setModel(readWriteTextModel);
        getInputMap().registerFunction(RichTextArea.Tag.PASTE, () -> {
            TextPos caretPosition = getCaretPosition();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString() && !clipboard.getString().isEmpty()) {
                readWriteTextModel.insertText(caretPosition, clipboard.getString());
                select(TextPos.ofLeading(caretPosition.index(), caretPosition.offset() + clipboard.getString().length()));
            }
            if (clipboard.hasImage()) {
                readWriteTextModel.insertImage(caretPosition, clipboard.getImage());
                select(TextPos.ofLeading(caretPosition.index(), caretPosition.offset() + "<image>".length()));
            }
        });

        if (AppResource.getTheme() == Theme.DARK) {
            MainApp.toggleStyleClass(this, MainApp.DARK_STYLE_CLASS, true);
            readWriteTextModel.setTextColor(Color.WHITE);
        }
        AppResource.themeProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                if (newValue == Theme.LIGHT) {
                    MainApp.toggleStyleClass(this, MainApp.DARK_STYLE_CLASS, false);
                    readWriteTextModel.setTextColor(Color.BLACK);
                } else {
                    MainApp.toggleStyleClass(this, MainApp.DARK_STYLE_CLASS, true);
                    readWriteTextModel.setTextColor(Color.WHITE);
                }
            }
        });
    }

    public List<ChatInputItem> getChatInputItems() {
        return readWriteTextModel.getChatInputItems();
    }

    public void removeRange(TextPos start, TextPos end) {
        readWriteTextModel.removeRange(start, end);
    }

    public void insertNewlineAt(TextPos pos) {
        // ReadWriteTextModel.insertLineBreak 已经实现了在任意位置分割段落
        readWriteTextModel.insertLineBreak(pos.index(), pos.offset());

        // 通知控件模型已变更
        // insertLineBreak 内部需要调用 fireChangeEvent，如果没有，在这里补发：
        int newIndex = pos.index() + 1;
        readWriteTextModel.fireChangeEvent(
                TextPos.ofLeading(pos.index(), pos.offset()),
                TextPos.ofLeading(newIndex, 0),
                0, 1, 0    // 插入了 1 个段落
        );
    }

}
