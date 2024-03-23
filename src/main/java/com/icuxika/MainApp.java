package com.icuxika;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.enums.ButtonType;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class MainApp extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppResource.setLanguage(Locale.SIMPLIFIED_CHINESE);

        Label label = new Label();
        label.textProperty().bind(AppResource.currentLocaleProperty().asString().concat(": ").concat(AppResource.getLanguageBinding("title")));

        MFXButton zhButton = new MFXButton("中文");
        zhButton.setPrefSize(120, 40);
        zhButton.setButtonType(ButtonType.FLAT);
        zhButton.setTextFill(Color.WHITE);
        zhButton.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, new CornerRadii(4), Insets.EMPTY)));
        zhButton.setOnAction(event -> AppResource.setLanguage(Locale.SIMPLIFIED_CHINESE));

        MFXButton enButton = new MFXButton("英文");
        enButton.setPrefSize(120, 40);
        enButton.setButtonType(ButtonType.FLAT);
        enButton.setTextFill(Color.WHITE);
        enButton.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, new CornerRadii(4), Insets.EMPTY)));
        enButton.setOnAction(event -> AppResource.setLanguage(Locale.ENGLISH));

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.getChildren().addAll(label, zhButton, enButton);

        primaryStage.titleProperty().bind(AppResource.getLanguageBinding("title"));
        primaryStage.setScene(new Scene(vBox, 600, 400));
        primaryStage.show();

        LOGGER.trace("[trace]日志控制台输出");
        LOGGER.debug("[debug]日志控制台输出");
        LOGGER.info("[info]日志记录到logs/application.log中");
        LOGGER.warn("[warn]日志记录到logs/application.log中");
        LOGGER.error("[error]日志记录到logs/application.log中");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
