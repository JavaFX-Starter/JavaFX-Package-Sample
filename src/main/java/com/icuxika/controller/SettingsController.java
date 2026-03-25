package com.icuxika.controller;

import com.icuxika.AppResource;
import com.icuxika.MainApp;
import com.icuxika.cell.LanguageCell;
import com.icuxika.constant.Theme;
import com.icuxika.jni.NativeFXWindow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private BorderPane rootContainer;

    @FXML
    private ToggleGroup themeToggleGroup;
    @FXML
    private ToggleButton systemThemeButton;
    @FXML
    private ToggleButton lightThemeButton;
    @FXML
    private ToggleButton darkThemeButton;

    @FXML
    private ComboBox<Locale> languageComboBox;

    @FXML
    private CheckBox startOnBootCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        switch (AppResource.getAvailableTheme()) {
            case SYSTEM -> themeToggleGroup.selectToggle(systemThemeButton);
            case LIGHT -> themeToggleGroup.selectToggle(lightThemeButton);
            case DARK -> themeToggleGroup.selectToggle(darkThemeButton);
        }
        themeToggleGroup.selectedToggleProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                switch (newValue) {
                    case Toggle t when t.equals(systemThemeButton) -> AppResource.setAvailableTheme(Theme.SYSTEM);
                    case Toggle t when t.equals(lightThemeButton) -> AppResource.setAvailableTheme(Theme.LIGHT);
                    case Toggle t when t.equals(darkThemeButton) -> AppResource.setAvailableTheme(Theme.DARK);
                    default -> throw new IllegalStateException("Unexpected value: " + newValue);
                }
            }
        });

        languageComboBox.setItems(FXCollections.observableList(AppResource.SUPPORT_LANGUAGE_LIST));
        languageComboBox.valueProperty().subscribe(locale -> {
            if (locale != null) {
                AppResource.setLanguage(locale);
            }
        });
        languageComboBox.valueProperty().bindBidirectional(AppResource.currentLocaleProperty());
        languageComboBox.setCellFactory(_ -> new LanguageCell());
        languageComboBox.setButtonCell(new LanguageCell());

        if (MainApp.isProductionMode()) {
            startOnBootCheckBox.setSelected(NativeFXWindow.isStartupEnable("JavaFXPackageSample"));
        } else {
            startOnBootCheckBox.setDisable(true);
        }
        startOnBootCheckBox.selectedProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                NativeFXWindow.setStartup("JavaFXPackageSample", newValue);
            }
        });
    }
}
