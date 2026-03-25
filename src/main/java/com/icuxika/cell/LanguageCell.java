package com.icuxika.cell;

import com.icuxika.AppResource;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.ListCell;

import java.util.Locale;

public class LanguageCell extends ListCell<Locale> {
    final StringBinding simplifiedChineseBinding = AppResource.getLanguageBinding("lang-zh-CN");
    final StringBinding englishBinding = AppResource.getLanguageBinding("lang-en");

    @Override
    protected void updateItem(Locale item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
        } else {
            textProperty().unbind();
            switch (item) {
                case Locale l when l.equals(Locale.SIMPLIFIED_CHINESE) -> textProperty().bind(simplifiedChineseBinding);
                case Locale l when l.equals(Locale.ENGLISH) -> textProperty().bind(englishBinding);
                default -> throw new IllegalStateException("暂不支持此区域: " + item);
            }
        }
    }
}
