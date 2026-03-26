package com.icuxika;

import com.icuxika.cell.LanguageCell;
import com.icuxika.cell.ThemeCell;
import com.icuxika.constant.Theme;
import com.icuxika.jni.NativeFXWindow;
import com.icuxika.lsp.LSPAgent;
import com.icuxika.richtext.LSPCodeArea;
import com.icuxika.richtext.TextFlowSyntaxDecorator;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.beans.binding.When;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.icuxika.FXUtil.toggleStyleClass;
import static com.icuxika.constant.SystemConstant.DARK_STYLE_CLASS;

public class MainApp extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 需要创建一份内容与LanguageResource.properties一致的LanguageResource_zh_CN.properties文件，否则在不是中文作为系统语言的操作系统上，中文语言绑定将无法正常运行
        // 同时最好准备一份字体用来渲染文字，沙盒中测试缺少字体的情况中文文字无法显示
        AppResource.setLanguage(Locale.SIMPLIFIED_CHINESE);
        AppResource.setAvailableTheme(Theme.LIGHT);

        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();

        Label label = new Label();
        label.textProperty().bind(AppResource.currentLocaleProperty().asString().concat(": ").concat(AppResource.getLanguageBinding("title")));

        LSPCodeArea lspCodeArea = new LSPCodeArea(AppResource.getTheme() == Theme.LIGHT, LSPAgent.DEMO_CODE);
        Button testButton = new Button("测试");

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(8);
        vBox.getChildren().addAll(
                label, createThemeComboBox(), createLanguageComboBox(),
                testButton, lspCodeArea,
                createTextFlow(true), createTextFlow(false)
        );

        HeaderBar headerBar = createHeaderBar(primaryStage);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(headerBar);
        borderPane.setCenter(vBox);

        var root = new StackPane();
        root.getChildren().add(borderPane);
        Scene scene = new Scene(root, 480, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/main.css")).toExternalForm());

        // 使 iconify, maximize, close 三个 HeaderBar 标题栏按钮响应主题变化
        AppResource.themeProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                if (newValue == Theme.LIGHT) {
                    scene.setFill(Color.WHITE);
                    toggleStyleClass(headerBar, DARK_STYLE_CLASS, false);
                } else {
                    scene.setFill(Color.BLACK);
                    toggleStyleClass(headerBar, DARK_STYLE_CLASS, true);
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.EXTENDED);
        primaryStage.show();

        testButton.setOnAction(event -> {
//            lspCodeArea.applyChange(new DiagnosticMessage(
//                    8, 25, 5, 26, "Syntax error, insert \";\" to complete BlockStatements"
//            ));
//            new Thread(() -> {
//                try {
//                    Thread.sleep(2000);
//                    FXUtil.runInFX(lspCodeArea::clearLastChange);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }).start();
            showChatPane(primaryStage);
        });
        // 启动语言服务器
//        lspCodeArea.startLanguageServer();
        // 程序退出时停止语言服务器
//        primaryStage.setOnCloseRequest(_ -> lspCodeArea.stopLanguageServer());

        LOGGER.trace("[trace]日志控制台输出");
        LOGGER.debug("[debug]日志控制台输出");
        LOGGER.info("[info]日志记录到logs/application.log中");
        LOGGER.warn("[warn]日志记录到logs/application.log中");
        LOGGER.error("[error]日志记录到logs/application.log中");
    }

    private void showSettingsPane(Stage primaryStage) {
        showModalPane(primaryStage, "settings", "设置");
    }

    private void showChatPane(Stage primaryStage) {
        showModalPane(primaryStage, "chat", "聊天");
    }

    private void showModalPane(Stage primaryStage, String key, String titleLabel) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fxml/" + key + ".fxml"));
        BorderPane rootContainer;
        try {
            rootContainer = loader.load();
            rootContainer.getStyleClass().add("modal-root");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HeaderBar headerBar = new HeaderBar();
        headerBar.getStyleClass().add("header-bar");

        Label label = new Label(titleLabel);
        label.getStyleClass().add("title-label");

        headerBar.setCenter(label);
        headerBar.setTrailing(createThemeButton());
        rootContainer.setTop(headerBar);
        Scene scene = new Scene(rootContainer, 600, 640);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/" + key + ".css")).toExternalForm());

        if (AppResource.getTheme() == Theme.DARK) {
            scene.setFill(Color.BLACK);
            toggleStyleClass(headerBar, DARK_STYLE_CLASS, true);
            toggleStyleClass(rootContainer, DARK_STYLE_CLASS, true);
        }
        AppResource.themeProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                if (newValue == Theme.LIGHT) {
                    scene.setFill(Color.WHITE);
                    toggleStyleClass(headerBar, DARK_STYLE_CLASS, false);
                    toggleStyleClass(rootContainer, DARK_STYLE_CLASS, false);
                } else {
                    scene.setFill(Color.BLACK);
                    toggleStyleClass(headerBar, DARK_STYLE_CLASS, true);
                    toggleStyleClass(rootContainer, DARK_STYLE_CLASS, true);
                }
            }
        });

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.initStyle(StageStyle.EXTENDED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
        stage.showAndWait();
    }


    private HeaderBar createHeaderBar(Stage primaryStage) {
        HeaderBar headerBar = new HeaderBar();
        headerBar.getStyleClass().add("header-bar");
        var leading = new HBox();
        var center = new HBox();
        var trailing = new HBox();

        ImageView icon = new ImageView();
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        icon.setImage(new Image("/application.png"));
        leading.setPrefWidth(24);
        leading.setAlignment(Pos.CENTER);
        leading.getChildren().addAll(icon);

        var appVersionLabel = new Label();
        appVersionLabel.getStyleClass().add("title-label");
        appVersionLabel.textProperty().bind(AppResource.getLanguageBinding("title").concat("(").concat(getAppVersion()).concat(")"));
        center.setAlignment(Pos.CENTER_LEFT);
        center.getChildren().add(appVersionLabel);

        Button themeButton = createThemeButton();
        Button pinToTopBtn = createPinToTopBtn(primaryStage);
        Button settingsButton = createSettingsButton(primaryStage);
        trailing.setPrefWidth(36);
        trailing.setAlignment(Pos.CENTER);
        trailing.getChildren().addAll(themeButton, pinToTopBtn, settingsButton);

        headerBar.setLeading(leading);
        headerBar.setCenter(center);
        headerBar.setTrailing(trailing);
        HeaderBar.setDragType(leading, HeaderDragType.DRAGGABLE_SUBTREE);
        HeaderBar.setDragType(center, HeaderDragType.DRAGGABLE_SUBTREE);
        return headerBar;
    }

    private Button createPinToTopBtn(Stage primaryStage) {
        Button pinToTopBtn = new Button();
        pinToTopBtn.getStyleClass().add("pin-to-top-button");
        var icon = new FontIcon(FontAwesomeSolid.THUMBTACK);
        icon.getStyleClass().add("pin-to-top-icon");
        pinToTopBtn.setGraphic(icon);
        pinToTopBtn.setOnAction(_ -> primaryStage.setAlwaysOnTop(!primaryStage.isAlwaysOnTop()));
        PseudoClass pinned = PseudoClass.getPseudoClass("pinned");
        primaryStage.alwaysOnTopProperty().addListener((_, _, newValue) -> {
            pinToTopBtn.pseudoClassStateChanged(pinned, newValue);
        });
        return pinToTopBtn;
    }

    private Button createThemeButton() {
        Button themeButton = new Button();
        themeButton.getStyleClass().add("theme-button");
        var sunnyIcon = new FontIcon(FontAwesomeSolid.SUN);
        sunnyIcon.getStyleClass().add("light-icon");
        var moonIcon = new FontIcon(FontAwesomeSolid.MOON);
        moonIcon.getStyleClass().add("dark-icon");
        themeButton.graphicProperty().bind(new When(AppResource.themeProperty().isEqualTo(Theme.LIGHT)).then(sunnyIcon).otherwise(moonIcon));
        themeButton.setOnAction(_ -> {
            if (AppResource.getAvailableTheme() == Theme.LIGHT) {
                AppResource.setAvailableTheme(Theme.DARK);
            } else {
                AppResource.setAvailableTheme(Theme.LIGHT);
            }
        });
        return themeButton;
    }

    private Button createSettingsButton(Stage primaryStage) {
        Button settingsButton = new Button();
        settingsButton.getStyleClass().add("settings-button");
        var settingsIcon = new FontIcon(FontAwesomeSolid.COG);
        settingsIcon.getStyleClass().add("settings-icon");
        settingsButton.setGraphic(settingsIcon);
        settingsButton.setOnAction(_ -> showSettingsPane(primaryStage));
        return settingsButton;
    }

    private ComboBox<Locale> createLanguageComboBox() {
        ComboBox<Locale> comboBox = new ComboBox<>(FXCollections.observableList(AppResource.SUPPORT_LANGUAGE_LIST));
        comboBox.valueProperty().subscribe(locale -> {
            if (locale != null) {
                AppResource.setLanguage(locale);
            }
        });
        comboBox.valueProperty().bindBidirectional(AppResource.currentLocaleProperty());
        comboBox.setCellFactory(_ -> new LanguageCell());
        comboBox.setButtonCell(new LanguageCell());
        return comboBox;
    }

    private ComboBox<Theme> createThemeComboBox() {
        ComboBox<Theme> comboBox = new ComboBox<>(FXCollections.observableList(List.of(Theme.SYSTEM, Theme.LIGHT, Theme.DARK)));
        comboBox.valueProperty().bindBidirectional(AppResource.availableThemeProperty());

        comboBox.setCellFactory(_ -> new ThemeCell());
        comboBox.setButtonCell(new ThemeCell());
        return comboBox;
    }

    private TextFlow createTextFlow(boolean isLight) {
        TextFlowSyntaxDecorator textFlowSyntaxDecorator = new TextFlowSyntaxDecorator(
                "/richtext/syntaxes/java.tmLanguage.json",
                isLight ? "/richtext/themes/light_vs.json" : "/richtext/themes/dark_vs.json"
        );
        return textFlowSyntaxDecorator.highlight("""
                package com.icuxika;
                
                public class Launcher {
                
                    static void main(String[] args) {
                        MainApp.main(args);
                    }
                }
                """);
    }

    /**
     * jpackage 生成应用程序映像时会在 app 目录下 .cfg 文件中写入应用程序版本
     */
    private String getAppVersion() {
        String appVersion = System.getProperty("jpackage.app-version");
        if (appVersion != null) {
            return appVersion;
        }
        return "开发版本";
    }

    public static boolean isProductionMode() {
        return System.getProperty("jpackage.app-version") != null;
    }

    public static void main(String[] args) {
        LOGGER.info("程序路径: {}", NativeFXWindow.getExecutablePath());
        // 启用 HeaderBar 预览功能
        System.setProperty("javafx.enablePreview", "true");
        System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(args);
    }
}
