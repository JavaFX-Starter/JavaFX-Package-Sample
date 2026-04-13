package com.icuxika.cell;

import com.icuxika.model.ChatMessage;
import com.icuxika.richtext.SelectableLabel;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.icuxika.FXUtil.createTextFlow;

public class ChatMessageCell extends ListCell<ChatMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatMessageCell.class);
    private static final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private Future<?> pendingImageLoadTask;
    private static final HttpClient httpClient = HttpClient.newBuilder().executor(executorService).build();
    private final ObjectProperty<Image> avatarImageProperty = new SimpleObjectProperty<>();
    private final StringProperty textProperty = new SimpleStringProperty();
    private final AnchorPane leftSelectableTextNode = createLeftSelectableTextNode();
    private final AnchorPane rightSelectableTextNode = createRightSelectableTextNode();
    private final AnchorPane leftImageNode = createLeftImageNode();
    private final AnchorPane rightImageNode = createRightImageNode();
    private ImageNodeInner leftImageNodeInner;
    private ImageNodeInner rightImageNodeInner;

    private ImageView createAvatarNode() {
        ImageView avatarNode = new ImageView();
        avatarNode.setFitWidth(36);
        avatarNode.setFitHeight(36);
        avatarNode.imageProperty().bind(avatarImageProperty);

        Rectangle avatarImageClip = new Rectangle(0, 0, avatarNode.getFitWidth(), avatarNode.getFitHeight());
        avatarImageClip.setArcWidth(8);
        avatarImageClip.setArcHeight(8);
        avatarNode.setClip(avatarImageClip);
        avatarNode.setEffect(new DropShadow(2, Color.BLACK));
        return avatarNode;
    }

    private SelectableLabel createSelectableText() {
        SelectableLabel selectableLabel = new SelectableLabel();
        selectableLabel.textProperty().bind(textProperty);
        return selectableLabel;
    }

    private AnchorPane createLeftSelectableTextNode() {
        ImageView avatar = createAvatarNode();
        SelectableLabel selectableLabel = createSelectableText();
        selectableLabel.getStyleClass().add("left-chat-bubble");
        TextFlow leftMsgDecorateTextFlow = createLeftMsgDecorateTextFlow();
        AnchorPane leftSelectableTextNode = new AnchorPane();
        setAnchor(avatar, true, selectableLabel, leftMsgDecorateTextFlow);
        leftSelectableTextNode.getChildren().addAll(avatar, selectableLabel, leftMsgDecorateTextFlow);
        return leftSelectableTextNode;
    }

    private AnchorPane createRightSelectableTextNode() {
        ImageView avatar = createAvatarNode();
        SelectableLabel selectableLabel = createSelectableText();
        selectableLabel.getStyleClass().add("right-chat-bubble");
        TextFlow rightMsgDecorateTextFlow = createRightMsgDecorateTextFlow();
        AnchorPane rightSelectableTextNode = new AnchorPane();
        setAnchor(avatar, false, selectableLabel, rightMsgDecorateTextFlow);
        rightSelectableTextNode.getChildren().addAll(avatar, selectableLabel, rightMsgDecorateTextFlow);
        return rightSelectableTextNode;
    }

    private AnchorPane createLeftImageNode() {
        ImageView avatar = createAvatarNode();
        if (leftImageNodeInner == null) {
            leftImageNodeInner = new ImageNodeInner();
        }
        TextFlow image = leftImageNodeInner;
        image.getStyleClass().add("left-chat-bubble");
        TextFlow leftMsgDecorateTextFlow = createLeftMsgDecorateTextFlow();
        AnchorPane leftImageNode = new AnchorPane();
        setAnchor(avatar, true, image, leftMsgDecorateTextFlow);
        leftImageNode.getChildren().addAll(avatar, image, leftMsgDecorateTextFlow);
        return leftImageNode;
    }

    private AnchorPane createRightImageNode() {
        ImageView avatar = createAvatarNode();
        if (rightImageNodeInner == null) {
            rightImageNodeInner = new ImageNodeInner();
        }
        TextFlow image = rightImageNodeInner;
        image.getStyleClass().add("right-chat-bubble");
        TextFlow rightMsgDecorateTextFlow = createRightMsgDecorateTextFlow();
        AnchorPane rightImageNode = new AnchorPane();
        setAnchor(avatar, false, image, rightMsgDecorateTextFlow);
        rightImageNode.getChildren().addAll(avatar, image, rightMsgDecorateTextFlow);
        return rightImageNode;
    }

    private TextFlow createLeftMsgDecorateTextFlow() {
        TextFlow leftMsgDecorateTextFlow = createTextFlow("M-0,0c0,565.161 458.839,1024 1024,1024l-0,-716.8c-408.482,0 -785.067,-137.652 -1024,-307.2Z", 24);
        leftMsgDecorateTextFlow.getStyleClass().add("left-msg-decorate");
        return leftMsgDecorateTextFlow;
    }

    private TextFlow createRightMsgDecorateTextFlow() {
        TextFlow rightMsgDecorateTextFlow = createTextFlow("M0,307.2l0,716.8c565.161,0 1024,-458.839 1024,-1024c-238.933,169.548 -615.518,307.2 -1024,307.2Z", 24);
        rightMsgDecorateTextFlow.getStyleClass().add("right-msg-decorate");
        return rightMsgDecorateTextFlow;
    }

    private void setAnchor(ImageView avatar, boolean isLeft, Node node, TextFlow decorate) {
        double vBarWidth = 12.0;
        AnchorPane.setTopAnchor(avatar, 8.0);
        AnchorPane.setTopAnchor(node, 8.0);
        if (isLeft) {
            AnchorPane.setLeftAnchor(avatar, 2.0 + vBarWidth);
            AnchorPane.setTopAnchor(decorate, 8.0);
            AnchorPane.setLeftAnchor(decorate, 40.0 + vBarWidth);
            AnchorPane.setLeftAnchor(node, 60.0 + vBarWidth);
        } else {
            AnchorPane.setRightAnchor(avatar, 2.0);
            AnchorPane.setTopAnchor(decorate, 8.0);
            AnchorPane.setRightAnchor(decorate, 40.0);
            AnchorPane.setRightAnchor(node, 60.0);
        }
    }

    @Override
    protected void updateItem(ChatMessage item, boolean empty) {
        super.updateItem(item, empty);

        if (pendingImageLoadTask != null && !pendingImageLoadTask.isDone()) {
            pendingImageLoadTask.cancel(true);
            pendingImageLoadTask = null;
        }

        if (item == null || empty) {
            setGraphic(null);
        } else {
            setText(null);
            if (avatarImageProperty.get() == null || !avatarImageProperty.get().getUrl().equals(item.getAvatar())) {
                Image image = new Image(item.getAvatar(), true);
                avatarImageProperty.set(image);
            }
            switch (item.getType()) {
                case SELECTABLE_TEXT -> {
                    textProperty.unbind();
                    textProperty.bind(item.msgProperty());
                    if (item.getLeft()) {
                        setGraphic(leftSelectableTextNode);
                    } else {
                        setGraphic(rightSelectableTextNode);
                    }
                }
                case IMAGE -> {
                    if (item.getLeft()) {
                        leftImageNodeInner.unbind();
                        leftImageNodeInner.bind(item);
                        setGraphic(leftImageNode);
                    } else {
                        rightImageNodeInner.unbind();
                        rightImageNodeInner.bind(item);
                        setGraphic(rightImageNode);
                    }
                    if (item.imageProperty().get() == null) {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(item.getImageUrl()))
                                .GET()
                                .build();
                        pendingImageLoadTask = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                                .thenAcceptAsync(inputStreamHttpResponse -> {
                                    System.out.println(Thread.currentThread().toString());
                                    var contentLength = inputStreamHttpResponse.headers().firstValueAsLong("Content-Length").orElse(-1);
                                    System.out.println(item.getImageUrl() + ": " + contentLength);
                                    try (
                                            InputStream is = inputStreamHttpResponse.body();
                                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                                    ) {
                                        byte[] data = new byte[8192];
                                        long workDone = 0;
                                        int n;
                                        long lastUpdate = 0;
                                        while ((n = is.read(data)) != -1) {
                                            os.write(data, 0, n);
                                            workDone += n;
                                            if (contentLength > 0) {
                                                if (System.currentTimeMillis() - lastUpdate > 50) {
                                                    System.out.println("progress: " + workDone + " / " + contentLength);
                                                    long finalWorkDone = workDone;
                                                    Platform.runLater(() -> item.imageProgressProperty().set((double) finalWorkDone / contentLength));
                                                    lastUpdate = System.currentTimeMillis();
                                                }
                                            }
                                        }
                                        Image image = new Image(new ByteArrayInputStream(os.toByteArray()));
                                        if (image.isError()) {
                                            Platform.runLater(() -> {
                                                item.imageProgressVisibleProperty().set(false);
                                                item.imageErrorVisibleProperty().set(true);
                                                item.imageErrorTextProperty().set("图片解析出错: " + image.getException().getMessage());
                                            });
                                            return;
                                        } else {
                                            Platform.runLater(() -> item.imageProgressVisibleProperty().set(false));
                                        }
                                        Platform.runLater(() -> {
                                            item.fitWidthProperty().set(image.getWidth() > 240 ? 240 : image.getWidth());
                                            item.imageProperty().set(image);
                                        });
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }, executorService)
                                .exceptionallyAsync(throwable -> {
                                    Platform.runLater(() -> {
                                        item.imageProgressVisibleProperty().set(false);
                                        item.imageErrorTextProperty().set("图片加载出错");
                                        item.imageErrorVisibleProperty().set(true);
                                    });
                                    LOGGER.warn(throwable.getMessage(), throwable);
                                    return null;
                                }, executorService);
                    }
                }
            }
        }
    }

    private static class ImageNodeInner extends TextFlow {
        private final ProgressBar progressBar = new ProgressBar();
        private final TextFlow error = new TextFlow();
        private final Text text = new Text();
        private final ImageView imageView = new ImageView();

        public ImageNodeInner() {
            text.setFill(Color.RED);
            error.getChildren().add(text);

            imageView.setPreserveRatio(true);
            imageView.setCache(true);

            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(imageView, error, progressBar);
            getStyleClass().add("chat-bubble");
            getChildren().add(stackPane);
        }

        public void bind(ChatMessage item) {
            progressBar.progressProperty().bind(item.imageProgressProperty());
            progressBar.visibleProperty().bind(item.imageProgressVisibleProperty());
            text.textProperty().bind(item.imageErrorTextProperty());
            error.visibleProperty().bind(item.imageErrorVisibleProperty());
            imageView.imageProperty().bind(item.imageProperty());
            imageView.fitWidthProperty().bind(item.fitWidthProperty());
        }

        public void unbind() {
            progressBar.progressProperty().unbind();
            progressBar.visibleProperty().unbind();
            text.textProperty().unbind();
            error.visibleProperty().unbind();
            imageView.imageProperty().unbind();
            imageView.fitWidthProperty().unbind();
        }
    }
}
