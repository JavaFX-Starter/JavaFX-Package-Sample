package com.icuxika;

import com.icuxika.constant.Theme;
import com.icuxika.lsp.DiagnosticMessage;
import com.icuxika.lsp.LSPAgent;
import com.icuxika.richtext.LSPCodeArea;
import com.icuxika.richtext.TextFlowSyntaxDecorator;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.application.Application;
import javafx.beans.binding.When;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import org.kordamp.ikonli.fluentui.FluentUiRegularMZ;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;

public class MainApp extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 需要创建一份内容与LanguageResource.properties一致的LanguageResource_zh_CN.properties文件，否则在不是中文作为系统语言的操作系统上，中文语言绑定将无法正常运行
        // 同时最好准备一份字体用来渲染文字，沙盒中测试缺少字体的情况中文文字无法显示
        AppResource.setLanguage(Locale.SIMPLIFIED_CHINESE);
        AppResource.setTheme(Theme.LIGHT);

        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();

        Label label = new Label();
        label.textProperty().bind(AppResource.currentLocaleProperty().asString().concat(": ").concat(AppResource.getLanguageBinding("title")));

        LSPCodeArea lspCodeArea = new LSPCodeArea(true, LSPAgent.DEMO_CODE);
        Button testButton = new Button("测试");

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(8);
        vBox.getChildren().addAll(
                label, createComboBox(),
                testButton, lspCodeArea,
                new LSPCodeArea(false, LSPAgent.DEMO_CODE),
                createTextFlow(true), createTextFlow(false)
        );

        HeaderBar headerBar = new HeaderBar();
        var leading = new Pane();
        leading.setPrefWidth(36);
        leading.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        headerBar.setLeading(leading);

        var center = new StackPane();
        center.setBackground(new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
        headerBar.setCenter(center);

        var appVersionLabel = new Label(getAppVersion());
        center.getChildren().add(appVersionLabel);

        var trailing = new StackPane();
        trailing.setPrefWidth(36);
        headerBar.setTrailing(trailing);

        HBox trailingWrapper = new HBox();
        trailingWrapper.setAlignment(Pos.CENTER);

        Button themeButton = new Button();
        var sunnyIcon = new FontIcon(FluentUiRegularMZ.WEATHER_SUNNY_24);
        sunnyIcon.setIconSize(16);
        sunnyIcon.setIconColor(Color.RED);
        var moonIcon = new FontIcon(FluentUiRegularMZ.WEATHER_MOON_24);
        moonIcon.setIconSize(16);
        moonIcon.setIconColor(Color.YELLOW);
        themeButton.setStyle("-fx-background-color: transparent;");
        themeButton.graphicProperty().bind(new When(AppResource.themeProperty().isEqualTo(Theme.LIGHT)).then(sunnyIcon).otherwise(moonIcon));
        themeButton.hoverProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                themeButton.setStyle("-fx-background-color: rgba(0,0,0,0.1);");
            } else {
                themeButton.setStyle("-fx-background-color: transparent;");
            }
        });
        themeButton.setOnAction(_ -> {
            if (AppResource.getTheme() == Theme.LIGHT) {
                AppResource.setTheme(Theme.DARK);
            } else {
                AppResource.setTheme(Theme.LIGHT);
            }
        });

        Button pinToTopBtn = new Button();
        var icon = new FontIcon(FluentUiRegularMZ.PIN_12);
        icon.setIconSize(16);
        pinToTopBtn.setStyle("-fx-background-color: transparent;");
        pinToTopBtn.setGraphic(icon);
        pinToTopBtn.hoverProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                pinToTopBtn.setStyle("-fx-background-color: rgba(0,0,0,0.1);");
            } else {
                pinToTopBtn.setStyle("-fx-background-color: transparent;");
            }
        });
        pinToTopBtn.setOnAction(_ -> {
            primaryStage.setAlwaysOnTop(!primaryStage.isAlwaysOnTop());
            if (primaryStage.isAlwaysOnTop()) {
                icon.setIconColor(Color.DODGERBLUE);
            } else {
                icon.setIconColor(Color.BLACK);
            }
        });
        trailingWrapper.getChildren().addAll(themeButton, pinToTopBtn);
        trailing.getChildren().add(trailingWrapper);

        HeaderBar.setDragType(leading, HeaderDragType.DRAGGABLE_SUBTREE);
        HeaderBar.setDragType(center, HeaderDragType.DRAGGABLE_SUBTREE);

        var root = new BorderPane();
        root.setTop(headerBar);
        root.setCenter(vBox);

        Scene scene = new Scene(root, 400, 800);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/main.css")).toExternalForm());
        primaryStage.titleProperty().bind(AppResource.getLanguageBinding("title"));
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.EXTENDED);
        primaryStage.show();

        testButton.setOnAction(event -> {
            lspCodeArea.applyChange(new DiagnosticMessage(
                    5, 25, 5, 26, "Syntax error, insert \";\" to complete BlockStatements"
            ));
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    FXUtil.runInFX(lspCodeArea::clearLastChange);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });
        lspCodeArea.startLanguageServer();
        primaryStage.setOnCloseRequest(_ -> lspCodeArea.stopLanguageServer());

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

    public static void main(String[] args) {
        // 启用 HeaderBar 预览功能
        System.setProperty("javafx.enablePreview", "true");
        System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(args);
    }
}
