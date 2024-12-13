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
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
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

        MFXButton enButton = createButton("English");
        enButton.setOnAction(_ -> AppResource.setLanguage(Locale.ENGLISH));

        MFXToggleButton mfxToggleButton = new MFXToggleButton();
        mfxToggleButton.textProperty().bind(new When(mfxToggleButton.selectedProperty().isEqualTo(new SimpleBooleanProperty(true))).then("监听 Ctrl + Alt + Z").otherwise("取消监听 Ctrl + Alt + Z"));
        mfxToggleButton.setDisable(true);

        GlobalKeyEvent globalKeyEvent = new GlobalKeyEvent().combine(0xA2).combine(0xA4).combine(KeyCode.Z.getCode()).onAction(() -> Platform.runLater(() -> {
            primaryStage.setIconified(!primaryStage.isIconified());
            primaryStage.toFront();
        }));

        MFXButton addKeyEventButton = createButton("添加全局键盘事件");
        addKeyEventButton.textProperty().bind(AppResource.getLanguageBinding("add-global-key-event-listening"));
        addKeyEventButton.setOnAction(_ -> {
            GlobalKeyboardListener.registerGlobalKeyEvent(globalKeyEvent);
            mfxToggleButton.setSelected(true);
        });

        MFXButton removeKeyEventButton = createButton("移除全局键盘事件");
        removeKeyEventButton.textProperty().bind(AppResource.getLanguageBinding("remove-global-key-event-listening"));
        removeKeyEventButton.setOnAction(_ -> {
            GlobalKeyboardListener.unregisterGlobalKeyEvent(globalKeyEvent.getId());
            mfxToggleButton.setSelected(false);
        });

        NativeFXWindowWrapper nativeFXWindow = new NativeFXWindowWrapper();

        SimpleStringProperty hWndProperty = new SimpleStringProperty();
        Label hWndLabel = createLabel();
        hWndLabel.textProperty().bind(new SimpleStringProperty("Win32 hWnd: ").concat(hWndProperty));

        SimpleStringProperty classNameProperty = new SimpleStringProperty();
        Label classNameLabel = createLabel();
        classNameLabel.textProperty().bind(new SimpleStringProperty("Win32 lpClassName: ").concat(classNameProperty));

        SimpleStringProperty windowNameProperty = new SimpleStringProperty();
        Label windowNameLabel = createLabel();
        windowNameLabel.textProperty().bind(new SimpleStringProperty("Win32 lpWindowName: ").concat(windowNameProperty));

        MFXButton setTransparencyButton = createButton("设置窗口半透明");
        setTransparencyButton.textProperty().bind(AppResource.getLanguageBinding("set-window-transparency"));
        setTransparencyButton.setBackground(new Background(
                new BackgroundFill(
                        new LinearGradient(
                                0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE,
                                new Stop(0.0, new Color(0.83, 0.85, 0.87, 1.0)),
                                new Stop(1.0, new Color(0.24, 0.33, 0.41, 1.0))
                        ), CornerRadii.EMPTY, Insets.EMPTY
                )));
        setTransparencyButton.setOnAction(_ -> nativeFXWindow.setWindowTransparency());

        MFXButton unsetTransparencyButton = createButton("设置窗口不透明");
        unsetTransparencyButton.textProperty().bind(AppResource.getLanguageBinding("unset-window-transparency"));
        unsetTransparencyButton.setBackground(new Background(
                new BackgroundFill(
                        new LinearGradient(
                                0.0, 0.0, 1.0, 0.0, true, CycleMethod.NO_CYCLE,
                                new Stop(0.0, new Color(0.83, 0.85, 0.87, 1.0)),
                                new Stop(1.0, new Color(0.24, 0.33, 0.41, 1.0))
                        ), CornerRadii.EMPTY, Insets.EMPTY
                )));
        unsetTransparencyButton.setOnAction(_ -> nativeFXWindow.unsetWindowTransparency());

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.getChildren().addAll(
                label, zhButton, enButton, mfxToggleButton, addKeyEventButton, removeKeyEventButton,
                hWndLabel, classNameLabel, windowNameLabel,
                setTransparencyButton, unsetTransparencyButton
        );

        primaryStage.titleProperty().bind(AppResource.getLanguageBinding("title"));
        primaryStage.setScene(new Scene(vBox, 400, 600));
        primaryStage.show();

        nativeFXWindow.initialize(primaryStage);
        hWndProperty.set(String.valueOf(nativeFXWindow.getHWnd()));
        classNameProperty.set(nativeFXWindow.getClassName());
        windowNameProperty.set(nativeFXWindow.getWindowText());

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
    }

    private MFXButton createButton(String text) {
        MFXButton mfxButton = new MFXButton(text);
        mfxButton.setPrefHeight(40);
        mfxButton.setButtonType(ButtonType.FLAT);
        mfxButton.setTextFill(Color.WHITE);
        mfxButton.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, new CornerRadii(4), Insets.EMPTY)));
        return mfxButton;
    }

    private Label createLabel() {
        Label label = new Label();
        label.setPrefHeight(40);
        label.setPadding(new Insets(0, 4, 0, 4));
        label.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        label.setTextFill(Color.WHITE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
