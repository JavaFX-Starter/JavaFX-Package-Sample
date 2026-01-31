package com.icuxika.richtext;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.TextPos;

import java.util.List;

public class ChatInputTextArea extends RichTextArea {

    private final ReadWriteTextModel readWriteTextModel = new ReadWriteTextModel();

    public ChatInputTextArea() {
        setStyle("""
                -fx-font-family: "HarmonyOS Sans SC";
                -fx-font-size: 14;
                """);
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
        readWriteTextModel.addSegment("这是一个图片: ");
        readWriteTextModel.addNodeSegment(() -> {
            ImageView imageView = new ImageView();
            imageView.setImage(new Image("https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg", false));
            imageView.setFitHeight(64);
            imageView.setPreserveRatio(true);
            return imageView;
        });
        readWriteTextModel.nl();
        readWriteTextModel.addSegment("xxx");
        readWriteTextModel.nl();
        readWriteTextModel.addSegment("yyy");
    }

    public List<ChatInputItem> getChatInputItems() {
        return readWriteTextModel.getChatInputItems();
    }
}
