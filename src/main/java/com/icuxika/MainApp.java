package com.icuxika;

import com.icuxika.jni.NativeFXWindowWrapper;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import io.github.palexdev.materialfx.enums.ButtonType;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
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

        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();

        Label label = new Label();
        label.textProperty().bind(AppResource.currentLocaleProperty().asString().concat(": ").concat(AppResource.getLanguageBinding("title")));

        MFXButton zhButton = createButton("中文");
        zhButton.setOnAction(_ -> AppResource.setLanguage(Locale.SIMPLIFIED_CHINESE));

        MFXButton enButton = createButton("英文");
        enButton.setOnAction(_ -> AppResource.setLanguage(Locale.ENGLISH));

        MFXToggleButton mfxToggleButton = new MFXToggleButton();
        mfxToggleButton.textProperty().bind(new When(mfxToggleButton.selectedProperty().isEqualTo(new SimpleBooleanProperty(true))).then("监听 Ctrl + Alt + Z").otherwise("取消监听 Ctrl + Alt + Z"));
        mfxToggleButton.setDisable(true);

        GlobalKeyEvent globalKeyEvent = new GlobalKeyEvent().combine(0xA2).combine(0xA4).combine(KeyCode.Z.getCode()).onAction(() -> Platform.runLater(() -> {
            primaryStage.setIconified(!primaryStage.isIconified());
            primaryStage.toFront();
        }));

        MFXButton addKeyEventButton = createButton("添加全局键盘事件");
        addKeyEventButton.setOnAction(_ -> {
            GlobalKeyboardListener.registerGlobalKeyEvent(globalKeyEvent);
            mfxToggleButton.setSelected(true);
        });

        MFXButton removeKeyEventButton = createButton("移除全局键盘事件");
        removeKeyEventButton.setOnAction(_ -> {
            GlobalKeyboardListener.unregisterGlobalKeyEvent(globalKeyEvent.getId());
            mfxToggleButton.setSelected(false);
        });

        Label hWndLabel = new Label();
        hWndLabel.setPrefSize(120, 40);
        hWndLabel.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        hWndLabel.setTextFill(Color.WHITE);
        hWndLabel.setAlignment(Pos.CENTER);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.getChildren().addAll(label, zhButton, enButton, mfxToggleButton, addKeyEventButton, removeKeyEventButton, hWndLabel);

        primaryStage.titleProperty().bind(AppResource.getLanguageBinding("title"));
        primaryStage.setScene(new Scene(vBox, 400, 600));
        primaryStage.show();

        // 挂载全局键盘事件监听钩子
        GlobalKeyboardListener globalKeyboardListener = new GlobalKeyboardListener();
        globalKeyboardListener.hook();
        // 窗口关闭时，卸载全局键盘事件监听钩子
        primaryStage.setOnCloseRequest(_ -> globalKeyboardListener.stop());

        LOGGER.trace("[trace]日志控制台输出");
        LOGGER.debug("[debug]日志控制台输出");
        LOGGER.info("[info]日志记录到logs/application.log中");
        LOGGER.warn("[warn]日志记录到logs/application.log中");
        LOGGER.error("[error]日志记录到logs/application.log中");

        NativeFXWindowWrapper nativeFXWindow = new NativeFXWindowWrapper(primaryStage);
        hWndLabel.setText(String.valueOf(nativeFXWindow.getHWnd()));
        LOGGER.info("window title: {}", nativeFXWindow.getWindowText());
        LOGGER.info("window class: {}", nativeFXWindow.getClassName());
    }

    private MFXButton createButton(String text) {
        MFXButton mfxButton = new MFXButton(text);
        mfxButton.setPrefSize(120, 40);
        mfxButton.setButtonType(ButtonType.FLAT);
        mfxButton.setTextFill(Color.WHITE);
        mfxButton.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, new CornerRadii(4), Insets.EMPTY)));
        return mfxButton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
