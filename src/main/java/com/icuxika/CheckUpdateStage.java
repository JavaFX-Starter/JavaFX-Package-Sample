package com.icuxika;

import com.google.gson.Gson;
import com.icuxika.model.FileInfo;
import com.icuxika.model.UpdateResult;
import com.icuxika.task.FileListDownloadTask;
import com.icuxika.task.UpdateResultDownloadTask;
import com.icuxika.util.FormatUtil;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.enums.ButtonType;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CheckUpdateStage extends Stage {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckUpdateStage.class);

    private final Gson gson = new Gson();

    private UpdateResult cacheUpdateResult;

    public CheckUpdateStage(Stage owner) {
        StackPane center = new StackPane();
        Label label = new Label("检查更新");
        MFXScrollPane scrollPane = new MFXScrollPane();
        scrollPane.setContent(label);

        StackPane container = new StackPane();
        MFXProgressSpinner spinner = new MFXProgressSpinner();
        container.getChildren().add(spinner);
        container.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        center.getChildren().addAll(scrollPane, container);

        MFXButton updateButton = new MFXButton("一键升级");
        updateButton.setPrefWidth(84);
        updateButton.setButtonType(ButtonType.FLAT);
        updateButton.setTextFill(Color.WHITE);
        updateButton.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, new CornerRadii(4), Insets.EMPTY)));
        updateButton.setOnAction(_ -> {
            updateButton.setDisable(true);

            // 保存更新索引文件
            List<String> fileUrlList = Stream.concat(cacheUpdateResult.added().stream(), cacheUpdateResult.updated().stream())
                    .map(FileInfo::path)
                    .collect(Collectors.toList());
            System.out.println(fileUrlList);
            Path latestJsonPath = Paths.get(System.getenv("LOCALAPPDATA"), "JavaFXPackageSample").resolve("update").resolve("latest.json");
            try {
                Files.writeString(latestJsonPath, gson.toJson(cacheUpdateResult));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 下载
            Task<Void> task = new FileListDownloadTask(
                    "http://127.0.0.1:8080/JavaFXSample",
                    cacheUpdateResult.version(),
                    fileUrlList,
                    Paths.get(System.getenv("LOCALAPPDATA"), "JavaFXPackageSample").resolve("update"));
            task.setOnSucceeded(_ -> {
                updateButton.setDisable(false);
                // 重启
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    LOGGER.info("ShutdownHook 执行");
                    Path target;
                    try {
                        Path jarPath = Path.of(AppUpdateTool.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                        if (jarPath.toString().contains("classes")) {
                            target = Path.of(jarPath.toFile().getParent()).resolve("buildImage").resolve("JavaFXSample");
                        } else {
                            target = Path.of(jarPath.toFile().getParentFile().getParent());
                        }
                        System.out.println(target);
                        Path autoUpdateHelperExePath = target.resolve("auto-update-helper.exe");
                        LOGGER.info("auto-update-helper.exe 路径: {}", autoUpdateHelperExePath);
                        ProcessBuilder processBuilder = new ProcessBuilder(autoUpdateHelperExePath.toString(), "--launch");
                        processBuilder.start();
                    } catch (URISyntaxException | IOException e) {
                        LOGGER.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }));
                Platform.runLater(() -> {
                    close();
                    owner.close();
                    Platform.exit();
                });
            });
            new Thread(task).start();
        });

        HBox bottom = new HBox();
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(16));
        bottom.getChildren().add(updateButton);

        HeaderBar headerBar = new HeaderBar();
        BorderPane root = new BorderPane();
        root.setTop(headerBar);
        root.setCenter(center);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 640, 480);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("css/main.css")).toExternalForm());
        setScene(scene);

        setResizable(false);
        initStyle(StageStyle.EXTENDED);
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        Task<UpdateResult> task = new UpdateResultDownloadTask("http://127.0.0.1:8080/JavaFXSample");
        task.setOnSucceeded(_ -> {
            UpdateResult updateResult = task.getValue();
            cacheUpdateResult = updateResult;
            long totalSize = Stream.concat(updateResult.added().stream(), updateResult.updated().stream())
                    .mapToLong(FileInfo::size)
                    .sum();
            System.out.println(updateResult);
            System.out.println("需要下载的文件大小: " + FormatUtil.fileSize2String(totalSize));

            String addedFiles = updateResult.added().stream()
                    .map(FileInfo::path)
                    .map(path -> "  + " + path)
                    .collect(Collectors.joining("\n"));

            String updatedFiles = updateResult.updated().stream()
                    .map(FileInfo::path)
                    .map(path -> "  * " + path)
                    .collect(Collectors.joining("\n"));

            String deletedFiles = updateResult.deleted().stream()
                    .map(FileInfo::path)
                    .map(path -> "  - " + path)
                    .collect(Collectors.joining("\n"));

            label.setText("""
                    发现新的更新: %s
                       新增文件: %d 个
                       更新文件: %d 个
                       删除文件: %d 个
                    总大小: %s
                    
                    【新增文件】
                    %s
                    
                    【更新文件】
                    %s
                    
                    【删除文件】
                    %s
                    """.formatted(
                    updateResult.version(),
                    updateResult.added().size(),
                    updateResult.updated().size(),
                    updateResult.deleted().size(),
                    FormatUtil.fileSize2String(totalSize),
                    addedFiles.isEmpty() ? "(无)" : addedFiles,
                    updatedFiles.isEmpty() ? "(无)" : updatedFiles,
                    deletedFiles.isEmpty() ? "(无)" : deletedFiles
            ));
            scrollPane.toFront();
        });
        new Thread(task).start();
    }

}
