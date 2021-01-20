package com.icuxika;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // cannot access class com.sun.javafx.scene.control.behavior.TabPaneBehavior (in module javafx.controls) because module javafx.controls does not export com.sun.javafx.scene.control.behavior to module com.jfoenix
        JFXTabPane jfxTabPane = new JFXTabPane();

        StringProperty buttonNameProperty = new SimpleStringProperty("Hello, world");
        JFXButton button = new JFXButton();
        button.textProperty().bind(buttonNameProperty);
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setTextFill(Color.WHITE);
        button.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, new CornerRadii(4), Insets.EMPTY)));

        primaryStage.setScene(new Scene(jfxTabPane, 600, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
