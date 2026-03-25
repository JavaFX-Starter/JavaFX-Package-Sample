package com.icuxika.cell;

import com.icuxika.AppResource;
import com.icuxika.constant.Theme;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.ListCell;

public class ThemeCell extends ListCell<Theme> {
    final StringBinding systemBinding = AppResource.getLanguageBinding("theme-system");
    final StringBinding lightBinding = AppResource.getLanguageBinding("theme-light");
    final StringBinding darkBinding = AppResource.getLanguageBinding("theme-dark");

    @Override
    protected void updateItem(Theme item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
        } else {
            textProperty().unbind();
            switch (item) {
                case SYSTEM -> textProperty().bind(systemBinding);
                case LIGHT -> textProperty().bind(lightBinding);
                case DARK -> textProperty().bind(darkBinding);
            }
        }
    }
}
