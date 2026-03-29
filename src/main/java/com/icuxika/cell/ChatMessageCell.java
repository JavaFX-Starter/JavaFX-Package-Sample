package com.icuxika.cell;

import com.icuxika.model.ChatMessage;
import com.icuxika.richtext.SelectableLabel;
import javafx.beans.property.*;
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

import static com.icuxika.FXUtil.createTextFlow;

public class ChatMessageCell extends ListCell<ChatMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatMessageCell.class);
    private final ObjectProperty<Image> avatarImageProperty = new SimpleObjectProperty<>();
    private final StringProperty textProperty = new SimpleStringProperty();
    private ImageView avatarNode;
    private AnchorPane leftSelectableTextNode;
    private AnchorPane rightSelectableTextNode;
    private final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
    private final DoubleProperty fitWidthProperty = new SimpleDoubleProperty();
    private AnchorPane leftImageNode;
    private AnchorPane rightImageNode;
    private TextFlow leftMsgDecorateTextFlow;
    private TextFlow rightMsgDecorateTextFlow;

    private ImageView getAvatarNode() {
        if (avatarNode == null) {
            avatarNode = new ImageView();
            avatarNode.setFitWidth(36);
            avatarNode.setFitHeight(36);
            avatarNode.imageProperty().bind(avatarImageProperty);

            Rectangle avatarImageClip = new Rectangle(0, 0, avatarNode.getFitWidth(), avatarNode.getFitHeight());
            avatarImageClip.setArcWidth(8);
            avatarImageClip.setArcHeight(8);
            avatarNode.setClip(avatarImageClip);
            avatarNode.setEffect(new DropShadow(2, Color.BLACK));
        }
        return avatarNode;
    }

    private SelectableLabel createSelectableText() {
        SelectableLabel selectableLabel = new SelectableLabel();
        selectableLabel.textProperty().bind(textProperty);
        return selectableLabel;
    }

    private AnchorPane getLeftSelectableTextNode() {
        if (leftSelectableTextNode == null) {
            ImageView avatar = getAvatarNode();
            SelectableLabel selectableLabel = createSelectableText();
            selectableLabel.getStyleClass().add("left-chat-bubble");
            leftSelectableTextNode = new AnchorPane();
            setAnchor(true, selectableLabel);
            leftSelectableTextNode.getChildren().addAll(avatar, selectableLabel, getLeftMsgDecorateTextFlow());
        }
        return leftSelectableTextNode;
    }

    private AnchorPane getRightSelectableTextNode() {
        if (rightSelectableTextNode == null) {
            ImageView avatar = getAvatarNode();
            SelectableLabel selectableLabel = createSelectableText();
            selectableLabel.getStyleClass().add("right-chat-bubble");
            rightSelectableTextNode = new AnchorPane();
            setAnchor(false, selectableLabel);
            rightSelectableTextNode.getChildren().addAll(avatar, selectableLabel, getRightMsgDecorateTextFlow());
        }
        return rightSelectableTextNode;
    }

    private TextFlow createImage() {
        ProgressBar progressBar = new ProgressBar();

        TextFlow error = new TextFlow();
        Text text = new Text();
        text.setFill(Color.RED);
        error.getChildren().add(text);
        error.setVisible(false);

        imageProperty.addListener((_, _, newValue) -> {
            if (newValue != null) {

                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(newValue.progressProperty());
                progressBar.setVisible(true);

                newValue.progressProperty().addListener((_, _, progress) -> {
                    if ((double) progress == 1.0) {
                        progressBar.setVisible(false);

                        if (imageProperty.get().getWidth() > 240) {
                            fitWidthProperty.set(240);
                        }
                    }
                });
                newValue.exceptionProperty().addListener((_, _, exception) -> {
                    if (exception != null) {
                        error.setVisible(true);
                        text.setText("图片加载出错: " + exception.getMessage());
                    }
                });
            }
        });

        TextFlow textFlow = new TextFlow();
        textFlow.getStyleClass().add("chat-bubble");
        ImageView imageView = new ImageView();
        imageView.imageProperty().bind(imageProperty);
        imageView.fitWidthProperty().bind(fitWidthProperty);
        imageView.setPreserveRatio(true);
        imageView.setCache(true);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(imageView, error, progressBar);
        textFlow.getChildren().add(stackPane);
        return textFlow;
    }

    private AnchorPane getLeftImageNode() {
        if (leftImageNode == null) {
            ImageView avatar = getAvatarNode();
            TextFlow image = createImage();
            image.getStyleClass().add("left-chat-bubble");
            leftImageNode = new AnchorPane();
            setAnchor(true, image);
            leftImageNode.getChildren().addAll(avatar, image, getLeftMsgDecorateTextFlow());
        }
        return leftImageNode;
    }

    private AnchorPane getRightImageNode() {
        if (rightImageNode == null) {
            ImageView avatar = getAvatarNode();
            TextFlow image = createImage();
            image.getStyleClass().add("right-chat-bubble");
            rightImageNode = new AnchorPane();
            setAnchor(false, image);
            rightImageNode.getChildren().addAll(avatar, image, getRightMsgDecorateTextFlow());
        }
        return rightImageNode;
    }

    private TextFlow getLeftMsgDecorateTextFlow() {
        if (leftMsgDecorateTextFlow == null) {
            leftMsgDecorateTextFlow = createTextFlow("M-0,0c0,565.161 458.839,1024 1024,1024l-0,-716.8c-408.482,0 -785.067,-137.652 -1024,-307.2Z", 24);
            leftMsgDecorateTextFlow.getStyleClass().add("left-msg-decorate");
        }
        return leftMsgDecorateTextFlow;
    }

    private TextFlow getRightMsgDecorateTextFlow() {
        if (rightMsgDecorateTextFlow == null) {
            rightMsgDecorateTextFlow = createTextFlow("M0,307.2l0,716.8c565.161,0 1024,-458.839 1024,-1024c-238.933,169.548 -615.518,307.2 -1024,307.2Z", 24);
            rightMsgDecorateTextFlow.getStyleClass().add("right-msg-decorate");
        }
        return rightMsgDecorateTextFlow;
    }

    private void setAnchor(boolean isLeft, Node node) {
        double vBarWidth = 12.0;
        AnchorPane.setTopAnchor(getAvatarNode(), 8.0);
        AnchorPane.setTopAnchor(node, 8.0);
        if (isLeft) {
            AnchorPane.setLeftAnchor(getAvatarNode(), 2.0 + vBarWidth);
            AnchorPane.setTopAnchor(getLeftMsgDecorateTextFlow(), 8.0);
            AnchorPane.setLeftAnchor(getLeftMsgDecorateTextFlow(), 40.0 + vBarWidth);
            AnchorPane.setLeftAnchor(node, 60.0 + vBarWidth);
        } else {
            AnchorPane.setRightAnchor(getAvatarNode(), 2.0);
            AnchorPane.setTopAnchor(getRightMsgDecorateTextFlow(), 8.0);
            AnchorPane.setRightAnchor(getRightMsgDecorateTextFlow(), 40.0);
            AnchorPane.setRightAnchor(node, 60.0);
        }
    }

    @Override
    protected void updateItem(ChatMessage item, boolean empty) {
        super.updateItem(item, empty);

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
                        setGraphic(getLeftSelectableTextNode());
                    } else {
                        setGraphic(getRightSelectableTextNode());
                    }
                }
                case IMAGE -> {
                    if (item.getLeft()) {
                        setGraphic(getLeftImageNode());
                    } else {
                        setGraphic(getRightImageNode());
                    }
                    if (imageProperty.get() == null || !imageProperty.get().getUrl().equals(item.getImageUrl())) {
                        Image image = new Image(item.getImageUrl(), true);
                        imageProperty.set(image);
                    }
                }
            }
        }
    }
}
