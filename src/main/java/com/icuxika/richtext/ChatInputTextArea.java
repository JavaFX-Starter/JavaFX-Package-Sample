package com.icuxika.richtext;

import com.icuxika.AppResource;
import com.icuxika.constant.Theme;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.TextPos;

import java.util.List;

import static com.icuxika.FXUtil.toggleStyleClass;
import static com.icuxika.constant.SystemConstant.DARK_STYLE_CLASS;

public class ChatInputTextArea extends RichTextArea {

    private final ReadWriteTextModel readWriteTextModel = new ReadWriteTextModel();

    private final ContextMenu contextMenu = new ContextMenu();
    public ChatInputTextArea() {
        getStyleClass().add("chat-input-text-area");
        setModel(readWriteTextModel);
        getInputMap().registerFunction(RichTextArea.Tag.PASTE, () -> {
            TextPos caretPosition = getCaretPosition();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString() && !clipboard.getString().isEmpty()) {
                readWriteTextModel.insertText(caretPosition, clipboard.getString());
                select(TextPos.ofLeading(caretPosition.index(), caretPosition.offset() + clipboard.getString().length()));
            }
            if (clipboard.hasImage()) {
                readWriteTextModel.insertImage(caretPosition, clipboard.getImage(), 64);
                select(TextPos.ofLeading(caretPosition.index(), caretPosition.offset() + 1));
            }
        });

        Theme currentTheme = AppResource.getTheme();
        if (currentTheme == Theme.DARK) {
            toggleStyleClass(this, DARK_STYLE_CLASS, true);
            readWriteTextModel.setTextColor(Color.WHITE);
        } else {
            readWriteTextModel.setTextColor(Color.BLACK);
        }
        AppResource.themeProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                if (newValue == Theme.LIGHT) {
                    toggleStyleClass(this, DARK_STYLE_CLASS, false);
                    readWriteTextModel.setTextColor(Color.BLACK);
                } else {
                    toggleStyleClass(this, DARK_STYLE_CLASS, true);
                    readWriteTextModel.setTextColor(Color.WHITE);
                }
                readWriteTextModel.fireStyleChangeEvent(TextPos.ZERO, readWriteTextModel.getDocumentEnd());
            }
        });

        MenuItem copyMenuItem = new MenuItem("复制");
        copyMenuItem.setOnAction(_ -> copy());
        MenuItem pasteMenuItem = new MenuItem("粘贴");
        pasteMenuItem.setOnAction(_ -> paste());
        contextMenu.getItems().addAll(copyMenuItem, pasteMenuItem);

        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
            var selection = getSelection();
            if (selection == null) {
                selectAll();
            } else {
                TextPos min = selection.getMin();
                TextPos max = selection.getMax();

                TextPos textPos = getTextPosition(event.getScreenX(), event.getScreenY());
                if (textPos.compareTo(min) < 0 || textPos.compareTo(max) > 0) {
                    selectAll();
                }
            }

        });
        getInputMap().addHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            event.consume();
            contextMenu.show(ChatInputTextArea.this, event.getScreenX(), event.getScreenY());
        });
    }

    public List<ChatInputItem> getChatInputItems() {
        return readWriteTextModel.getChatInputItems();
    }

    public void removeRange(TextPos start, TextPos end) {
        readWriteTextModel.removeRange(start, end);
    }

    public void insertNewlineAt(TextPos pos) {
        readWriteTextModel.insertLineBreak(pos.index(), pos.offset());

        int newIndex = pos.index() + 1;
        readWriteTextModel.fireChangeEvent(
                TextPos.ofLeading(pos.index(), pos.offset()),
                TextPos.ofLeading(newIndex, 0),
                0, 1, 0
        );
        select(TextPos.ofLeading(newIndex, 0));
    }

    public void insertNewlineAtCaretPosition() {
        insertNewlineAt(getCaretPosition());
    }
}
