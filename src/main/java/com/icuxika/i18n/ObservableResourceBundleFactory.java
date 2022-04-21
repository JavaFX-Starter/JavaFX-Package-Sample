package com.icuxika.i18n;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ResourceBundle;

/**
 * 多国语言属性绑定
 */
public class ObservableResourceBundleFactory {

    private final ObjectProperty<ResourceBundle> resourceBundleProperty = new SimpleObjectProperty<>();

    public ObjectProperty<ResourceBundle> getResourceBundleProperty() {
        return resourceBundleProperty;
    }

    public ResourceBundle getResourceBundle() {
        return getResourceBundleProperty().get();
    }

    public void setResourceBundle(ResourceBundle resourceBundle) {
        getResourceBundleProperty().set(resourceBundle);
    }

    public StringBinding getStringBinding(String key) {
        return new StringBinding() {
            {
                bind(resourceBundleProperty);
            }

            @Override
            protected String computeValue() {
                return getResourceBundle().getString(key);
            }
        };
    }
}
