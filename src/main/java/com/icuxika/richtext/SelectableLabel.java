package com.icuxika.richtext;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.RichTextArea;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.StyleAttributeMap;

import java.util.ArrayList;
import java.util.List;

public class SelectableLabel extends RichTextArea {
    private final ContextMenu contextMenu = new ContextMenu();

    public SelectableLabel() {
        getStyleClass().add("selectable-label");
        setEditable(false);
        setWrapText(true);
        setDisplayCaret(false);
        setMaxWidth(360);
        setUseContentHeight(true);
        textProperty().subscribe(text -> {
            if (text != null) {
                clear();
                appendText(text, StyleAttributeMap.builder()
                        .setTextColor(getTextColor())
                        .setFontSize(getFontSize())
                        .setFontFamily(getFontFamily())
                        .build());
            }
        });
        textColorProperty().subscribe(color -> {
            if (color != null) {
                applyStyle(TextPos.ZERO, getDocumentEnd(), StyleAttributeMap.builder().setTextColor(color).build());
            }
        });

        MenuItem copyMenuItem = new MenuItem("复制");
        copyMenuItem.setOnAction(_ -> copy());
        contextMenu.getItems().addAll(copyMenuItem);

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
            contextMenu.show(SelectableLabel.this, event.getScreenX(), event.getScreenY());
        });
    }

    private final StringProperty text = new SimpleStringProperty();

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String value) {
        text.set(value);
    }

    public String getText() {
        return text.get();
    }

    private final StyleableObjectProperty<Color> textColor = new SimpleStyleableObjectProperty<>(StyleableProperties.TEXT_COLOR, SelectableLabel.this, "textColor");

    public StyleableObjectProperty<Color> textColorProperty() {
        return textColor;
    }

    public void setTextColor(Color color) {
        textColor.set(color);
    }

    public Color getTextColor() {
        return textColor.get();
    }

    private final DoubleProperty fontSize = new SimpleDoubleProperty(14);

    public DoubleProperty fontSizeProperty() {
        return fontSize;
    }

    public void setFontSize(double size) {
        fontSize.set(size);
    }

    public double getFontSize() {
        return fontSize.get();
    }

    private final StringProperty fontFamily = new SimpleStringProperty("HarmonyOS Sans SC");

    public StringProperty fontFamilyProperty() {
        return fontFamily;
    }

    public void setFontFamily(String value) {
        fontFamily.set(value);
    }

    public String getFontFamily() {
        return fontFamily.get();
    }

    private static class StyleableProperties {
        private static final CssMetaData<SelectableLabel, Color> TEXT_COLOR = new CssMetaData<>("-fx-selectable-label-text-color", StyleConverter.getColorConverter()) {
            @Override
            public boolean isSettable(SelectableLabel styleable) {
                return !styleable.textColor.isBound();
            }

            @Override
            public StyleableProperty<Color> getStyleableProperty(SelectableLabel styleable) {
                return styleable.textColorProperty();
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = List.of(TEXT_COLOR);
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        var controlCssMetaData = super.getControlCssMetaData();
        List<CssMetaData<? extends Styleable, ?>> combined = new ArrayList<>(controlCssMetaData.size() + StyleableProperties.STYLEABLES.size());
        combined.addAll(controlCssMetaData);
        combined.addAll(StyleableProperties.STYLEABLES);
        return combined;
    }
}
