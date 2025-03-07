package com.icuxika;

import com.icuxika.jni.NativeFXWindow;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import io.github.palexdev.materialfx.enums.ButtonType;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
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
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;

public class MainApp extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 需要创建一份内容与LanguageResource.properties一致的LanguageResource_zh_CN.properties文件，否则在不是中文作为系统语言的操作系统上，中文语言绑定将无法正常运行
        // 同时最好准备一份字体用来渲染文字，沙盒中测试缺少字体的情况中文文字无法显示
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

        NativeFXWindow nativeFXWindow = new NativeFXWindow();

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

        MFXButton loginButton = createButton("登录");
        loginButton.setOnAction(_ -> {
            // 测试 OAuth 2.0 登录逻辑
            // 服务端代码 https://github.com/icuxika/driftwood-cloud
            try {
                Desktop.getDesktop().browse(new URI("http://localhost:8900/oauth2/authorize?response_type=code&client_id=id_desktop_authorization_code"));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10);
        vBox.getChildren().addAll(
                label, createComboBox(),
                mfxToggleButton, addKeyEventButton, removeKeyEventButton,
                hWndLabel, classNameLabel, windowNameLabel,
                setTransparencyButton, unsetTransparencyButton,
                loginButton
        );

        Scene scene = new Scene(vBox, 400, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/main.css")).toExternalForm());
        primaryStage.titleProperty().bind(AppResource.getLanguageBinding("title"));
        primaryStage.setScene(scene);
        primaryStage.show();

        nativeFXWindow.initialize(primaryStage);
        hWndProperty.set(String.valueOf(nativeFXWindow.getHWnd()));
        classNameProperty.set(nativeFXWindow.getClassName());
        windowNameProperty.set(nativeFXWindow.getWindowText());

        primaryStage.titleProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                windowNameProperty.set(nativeFXWindow.getWindowText());
            }
        });

        // 挂载全局键盘事件监听钩子
        GlobalKeyboardListener globalKeyboardListener = new GlobalKeyboardListener();
        globalKeyboardListener.setCallback(() -> Platform.runLater(() -> {
            Label animationLabel = new Label("RegisterHotKey注册的快捷键被触发了");
            animationLabel.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
            animationLabel.setTextFill(Color.WHITE);
            vBox.getChildren().add(animationLabel);
            new Timeline(new KeyFrame(Duration.millis(1000), _ -> vBox.getChildren().remove(animationLabel), new KeyValue(animationLabel.opacityProperty(), 0))).play();
        }));
        globalKeyboardListener.hook();
        // 窗口关闭时，卸载全局键盘事件监听钩子
        primaryStage.setOnCloseRequest(_ -> globalKeyboardListener.stop());

        LOGGER.trace("[trace]日志控制台输出");
        LOGGER.debug("[debug]日志控制台输出");
        LOGGER.info("[info]日志记录到logs/application.log中");
        LOGGER.warn("[warn]日志记录到logs/application.log中");
        LOGGER.error("[error]日志记录到logs/application.log中");
    }

    private ComboBox<Locale> createComboBox() {
        ComboBox<Locale> comboBox = new ComboBox<>(FXCollections.observableList(AppResource.SUPPORT_LANGUAGE_LIST));
        comboBox.valueProperty().subscribe(locale -> {
            if (locale != null) {
                AppResource.setLanguage(locale);
            }
        });
        comboBox.valueProperty().bindBidirectional(AppResource.currentLocaleProperty());
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Locale locale) {
                String text;
                switch (locale) {
                    case Locale l when l.equals(Locale.SIMPLIFIED_CHINESE) ->
                            text = AppResource.getLanguageBinding("lang-zh-CN").get();
                    case Locale l when l.equals(Locale.ENGLISH) ->
                            text = AppResource.getLanguageBinding("lang-en").get();
                    default -> throw new IllegalStateException("暂不支持此区域: " + locale);
                }
                return text;
            }

            @Override
            public Locale fromString(String s) {
                Locale locale;
                switch (s) {
                    case String text when text.equals(AppResource.getLanguageBinding("lang-zh-CN").get()) ->
                            locale = Locale.SIMPLIFIED_CHINESE;
                    case String text when text.equals(AppResource.getLanguageBinding("lang-en").get()) ->
                            locale = Locale.ENGLISH;
                    default -> throw new IllegalStateException("暂不支持此区域: " + s);
                }
                return locale;
            }
        });
        return comboBox;
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
        if (!SingleInstanceManager.isFirstInstance(args)) {
            System.out.println("已有实例运行，本进程退出");
            System.exit(0);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(SingleInstanceManager::cleanup));

        launch(args);
    }
}
