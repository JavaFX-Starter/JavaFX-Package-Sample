package com.icuxika.controller;

import com.icuxika.richtext.ChatInputItem;
import com.icuxika.richtext.ChatInputTextArea;
import com.icuxika.richtext.SelectableLabel;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import jfx.incubator.scene.control.richtext.SelectionSegment;
import jfx.incubator.scene.control.richtext.TextPos;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
    @FXML
    private BorderPane rootContainer;
    @FXML
    private BorderPane contentContainer;

    private final ListView<Message> messageListView = new ListView<>();
    private final ObservableList<Message> messageObservableList = FXCollections.observableArrayList();

    private final ChatInputTextArea chatInputTextArea = new ChatInputTextArea();

    private final ObjectProperty<MessageSendType> messageSendType = new SimpleObjectProperty<>(MessageSendType.ENTER);

    private ObjectProperty<MessageSendType> messageSendTypeProperty() {
        return this.messageSendType;
    }

    public void setMessageSendType(MessageSendType value) {
        messageSendType.set(value);
    }

    public MessageSendType getMessageSendType() {
        return messageSendType.get();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageListView.getStyleClass().add("message-list-view");
        contentContainer.setCenter(messageListView);

        messageListView.setItems(messageObservableList);
        messageListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Message> call(ListView<Message> param) {
                return new ListCell<>() {
                    private final StringProperty textProperty = new SimpleStringProperty();
                    private AnchorPane leftSelectableTextNode;
                    private AnchorPane rightSelectableTextNode;
                    private final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
                    private final DoubleProperty fitWidthProperty = new SimpleDoubleProperty();
                    private AnchorPane leftImageNode;
                    private AnchorPane rightImageNode;
                    private TextFlow leftMsgDecorateTextFlow;
                    private TextFlow rightMsgDecorateTextFlow;

                    private SelectableLabel createSelectableText() {
                        SelectableLabel selectableLabel = new SelectableLabel();
                        selectableLabel.textProperty().bind(textProperty);
                        return selectableLabel;
                    }

                    private AnchorPane getLeftSelectableTextNode() {
                        if (leftSelectableTextNode == null) {
                            SelectableLabel selectableLabel = createSelectableText();
                            selectableLabel.getStyleClass().add("left-chat-bubble");
                            leftSelectableTextNode = new AnchorPane();
                            AnchorPane.setLeftAnchor(selectableLabel, 24.0);
                            AnchorPane.setTopAnchor(selectableLabel, 0.0);
                            AnchorPane.setLeftAnchor(getLeftMsgDecorateTextFlow(), 4.0);
                            AnchorPane.setTopAnchor(getLeftMsgDecorateTextFlow(), 2.0);
                            leftSelectableTextNode.getChildren().addAll(selectableLabel, getLeftMsgDecorateTextFlow());
                        }
                        return leftSelectableTextNode;
                    }

                    private AnchorPane getRightSelectableTextNode() {
                        if (rightMsgDecorateTextFlow == null) {
                            SelectableLabel selectableLabel = createSelectableText();
                            selectableLabel.getStyleClass().add("right-chat-bubble");
                            rightSelectableTextNode = new AnchorPane();
                            AnchorPane.setRightAnchor(selectableLabel, 24.0);
                            AnchorPane.setTopAnchor(selectableLabel, 0.0);
                            AnchorPane.setRightAnchor(getRightMsgDecorateTextFlow(), 4.0);
                            AnchorPane.setTopAnchor(getRightMsgDecorateTextFlow(), 2.0);
                            rightSelectableTextNode.getChildren().addAll(selectableLabel, getRightMsgDecorateTextFlow());
                        }
                        return rightSelectableTextNode;
                    }

                    private TextFlow createImage() {
                        TextFlow textFlow = new TextFlow();
                        textFlow.getStyleClass().add("chat-bubble");
                        ImageView imageView = new ImageView();
                        imageView.imageProperty().bind(imageProperty);
                        imageView.fitWidthProperty().bind(fitWidthProperty);
                        imageView.setPreserveRatio(true);
                        imageView.setCache(true);
                        textFlow.getChildren().add(imageView);
                        return textFlow;
                    }

                    private AnchorPane getLeftImageNode() {
                        if (leftImageNode == null) {
                            TextFlow image = createImage();
                            image.getStyleClass().add("left-chat-bubble");
                            leftImageNode = new AnchorPane();
                            AnchorPane.setLeftAnchor(image, 24.0);
                            AnchorPane.setTopAnchor(image, 0.0);
                            AnchorPane.setLeftAnchor(getLeftMsgDecorateTextFlow(), 4.0);
                            AnchorPane.setTopAnchor(getLeftMsgDecorateTextFlow(), 2.0);
                            leftImageNode.getChildren().addAll(image, getLeftMsgDecorateTextFlow());
                        }
                        return leftImageNode;
                    }

                    private AnchorPane getRightImageNode() {
                        if (rightImageNode == null) {
                            TextFlow image = createImage();
                            image.getStyleClass().add("right-chat-bubble");
                            rightImageNode = new AnchorPane();
                            AnchorPane.setRightAnchor(image, 24.0);
                            AnchorPane.setTopAnchor(image, 0.0);
                            AnchorPane.setRightAnchor(getRightMsgDecorateTextFlow(), 4.0);
                            AnchorPane.setTopAnchor(getRightMsgDecorateTextFlow(), 2.0);
                            rightImageNode.getChildren().addAll(image, getRightMsgDecorateTextFlow());
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

                    @Override
                    protected void updateItem(Message item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(null);
                            switch (item.type) {
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
                                    if (imageProperty.get() == null || !imageProperty.get().getUrl().equals(item.imageUrl)) {
                                        Image image = new Image(item.imageUrl, true);
                                        image.progressProperty().addListener((_, _, newValue) -> {
                                            if ((double) newValue == 1.0) {
                                                if (imageProperty.get().getWidth() > 240) {
                                                    fitWidthProperty.set(240);
                                                }
                                            }
                                        });
                                        image.exceptionProperty().addListener(new ChangeListener<Exception>() {
                                            @Override
                                            public void changed(ObservableValue<? extends Exception> observable, Exception oldValue, Exception newValue) {
                                                if (newValue != null) {
                                                    LOGGER.error(newValue.getMessage());
                                                }
                                            }
                                        });
                                        imageProperty.set(image);
                                    }
                                    if (item.left) {
                                        setGraphic(getLeftImageNode());
                                    } else {
                                        setGraphic(getRightImageNode());
                                    }
                                }
                            }
                        }
                    }
                };
            }
        });

        chatInputTextArea.addEventFilter(KeyEvent.KEY_PRESSED, new MessageSendKeyEventHandler(chatInputTextArea, this::sendMessage));

        Button sendMsgButton = new Button("发送");
        sendMsgButton.setOnAction(_ -> sendMessage());

        Label splitLabel = new Label("|");

        Button msgSendTypeButton = new Button();
        FontIcon angleUp = new FontIcon(FontAwesomeSolid.ANGLE_UP);
        angleUp.setIconColor(Color.WHITE);
        FontIcon angleDown = new FontIcon(FontAwesomeSolid.ANGLE_DOWN);
        angleDown.setIconColor(Color.WHITE);
        msgSendTypeButton.setGraphic(angleDown);
        ContextMenu contextMenu = buildMessageSendTypeContextMenu();
        contextMenu.setOnShowing(_ -> msgSendTypeButton.setGraphic(angleUp));
        contextMenu.setOnHidden(_ -> msgSendTypeButton.setGraphic(angleDown));
        msgSendTypeButton.setOnMouseClicked(event -> contextMenu.show(msgSendTypeButton, event.getScreenX(), event.getScreenY()));

        sendMsgButton.setBackground(Background.EMPTY);
        sendMsgButton.setTextFill(Color.WHITE);
        splitLabel.setTextFill(Color.WHITE);
        msgSendTypeButton.setBackground(Background.EMPTY);

        HBox sendMsgButtonContainer = new HBox();
        sendMsgButtonContainer.setSpacing(2);
        sendMsgButtonContainer.setAlignment(Pos.CENTER);
        sendMsgButtonContainer.setBackground(new Background(new BackgroundFill(Color.DODGERBLUE, new CornerRadii(4), Insets.EMPTY)));
        sendMsgButtonContainer.getChildren().addAll(sendMsgButton, splitLabel, msgSendTypeButton);

        HBox bottom = new HBox();
        bottom.getStyleClass().add("send-button-container");
        bottom.setPadding(new Insets(4, 4, 4, 0));
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.getChildren().add(sendMsgButtonContainer);

        VBox vBox = new VBox();
        VBox.setVgrow(chatInputTextArea, Priority.ALWAYS);
        vBox.getChildren().addAll(chatInputTextArea, bottom);
        contentContainer.setBottom(vBox);

        loadMessage();
    }

    private void loadMessage() {
        Message imageMsg1 = new Message();
        imageMsg1.setType(MessageType.IMAGE);
        messageObservableList.add(imageMsg1);

        Message msg1 = new Message();
        msg1.setLeft(true);
        msg1.setMsg("你要好好长大，不要输给风，不要输给雨，不要输给冬雪，不要输给炎夏。少年人，你在孩童时应当快乐，使你的心欢畅，行你所愿行的，见你所愿见的，然而也应当记住黑暗的时日。但愿新的梦想永远不被无留陀侵蚀，但愿旧的故事与无留陀一同被忘却，但愿绿色的原野，山丘永远不会变得枯黄，但愿溪水永远清澈，但愿鲜花永远盛开。挚友将再次同行于茂密的森林中，一切美好的事物终将归来，一切痛苦的记忆也会远去，就像溪水净化自己，枯树绽出新芽。最终，森林会记住一切。");
        messageObservableList.add(msg1);

        Message msg2 = new Message();
        msg2.setLeft(false);
        msg2.setMsg("如此绚丽的花朵，不该在绽放之前就枯萎。我会赠予你璀璨的祝福，而你的灵魂，也将绽放更耀眼的光辉。亲爱的山雀，请将我的箭，我的花，与我的爱，带给那孑然独行的旅人。愿你前行的道路有群星闪耀。愿你留下的足迹有百花绽放。你即是上帝的馈赠，世界因你而瑰丽。");
        messageObservableList.add(msg2);

        Message msg3 = new Message();
        msg3.setLeft(true);
        msg3.setMsg("你要好好长大，不要输给风，不要输给雨，不要输给冬雪，不要输给炎夏。少年人，你在孩童时应当快乐，使你的心欢畅，行你所愿行的，见你所愿见的，然而也应当记住黑暗的时日。但愿新的梦想永远不被无留陀侵蚀，但愿旧的故事与无留陀一同被忘却，但愿绿色的原野，山丘永远不会变得枯黄，但愿溪水永远清澈，但愿鲜花永远盛开。挚友将再次同行于茂密的森林中，一切美好的事物终将归来，一切痛苦的记忆也会远去，就像溪水净化自己，枯树绽出新芽。最终，森林会记住一切。");
        messageObservableList.add(msg3);

        Message msg4 = new Message();
        msg4.setLeft(false);
        msg4.setMsg("如此绚丽的花朵，不该在绽放之前就枯萎。我会赠予你璀璨的祝福，而你的灵魂，也将绽放更耀眼的光辉。亲爱的山雀，请将我的箭，我的花，与我的爱，带给那孑然独行的旅人。愿你前行的道路有群星闪耀。愿你留下的足迹有百花绽放。你即是上帝的馈赠，世界因你而瑰丽。");
        messageObservableList.add(msg4);
    }

    private void sendMessage() {
        List<ChatInputItem> items = chatInputTextArea.getChatInputItems();
        int index = 1;
        for (ChatInputItem item : items) {
            if (item instanceof ChatInputItem.Text(String text)) {
                System.out.println("文本==========");
                System.out.println(text);
                Message msg = new Message();
                msg.setLeft(false);
                msg.setMsg(text.trim());
                messageObservableList.add(msg);
                System.out.println("文本==========");
                // 发送文本消息...
            } else if (item instanceof ChatInputItem.ImageItem(Image image)) {
                System.out.println("图片: " + image);
                // 上传图片并发送...
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                try {
                    File file = new File("target\\" + index + ".png");
                    ImageIO.write(bufferedImage, "png", file);

                    Message msg = new Message();
                    msg.setLeft(false);
                    msg.setType(MessageType.IMAGE);
                    msg.setImageUrl("file:/" + file.getAbsolutePath());
                    messageObservableList.add(msg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                index++;
            }
        }
        messageListView.scrollTo(messageObservableList.size() - 1);
    }

    protected class MessageSendKeyEventHandler implements EventHandler<KeyEvent> {

        private final ChatInputTextArea chatInputTextArea;
        private final Runnable callback;

        public MessageSendKeyEventHandler(ChatInputTextArea chatInputTextArea, Runnable callback) {
            this.chatInputTextArea = chatInputTextArea;
            this.callback = callback;
        }

        @Override
        public void handle(KeyEvent event) {
            MessageSendType messageSendType = getMessageSendType();
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                boolean control = event.isControlDown();
                boolean shift = event.isShiftDown();
                if ((control && messageSendType == MessageSendType.CTRL_ENTER) || (!control && !shift && messageSendType == MessageSendType.ENTER)) {
                    callback.run();
                    chatInputTextArea.clear();
                } else {
                    insertNewline();
                }
            }
        }

        private void insertNewline() {
            SelectionSegment sel = chatInputTextArea.getSelection();
            TextPos min = sel.getMin();
            TextPos max = sel.getMax();
            // 如果有选区，先删除选区内容
            if (!min.equals(max)) {
                chatInputTextArea.removeRange(min, max);
            }
            chatInputTextArea.insertNewlineAt(min);
        }
    }

    private ContextMenu buildMessageSendTypeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        CheckMenuItem enterMenuItem = new CheckMenuItem("按Enter键发送消息");
        enterMenuItem.setSelected(true);
        enterMenuItem.setOnAction(event -> messageSendTypeProperty().set(MessageSendType.ENTER));
        CheckMenuItem ctrlEnterMenuItem = new CheckMenuItem("按Ctrl+Enter键发送消息");
        ctrlEnterMenuItem.setOnAction(event -> messageSendTypeProperty().set(MessageSendType.CTRL_ENTER));
        contextMenu.getItems().add(enterMenuItem);
        contextMenu.getItems().add(ctrlEnterMenuItem);
        messageSendTypeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                enterMenuItem.setSelected(newValue == MessageSendType.ENTER);
                ctrlEnterMenuItem.setSelected(newValue == MessageSendType.CTRL_ENTER);
            }
        });
        return contextMenu;
    }

    /**
     * 消息发送快捷方式
     */
    private enum MessageSendType {

        /**
         * 按Enter键发送消息
         */
        ENTER,

        /**
         * 按Ctrl+Enter键发送消息
         */
        CTRL_ENTER
    }

    private TextFlow createTextFlow(String svgContent, double size) {
        TextFlow textFlow = new TextFlow();
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(svgContent);
        textFlow.setShape(svgPath);
        textFlow.setMinWidth(size);
        textFlow.setMinHeight(size);
        textFlow.setMaxWidth(size);
        textFlow.setMaxHeight(size);
        return textFlow;
    }

    private enum MessageType {

        SELECTABLE_TEXT,

        IMAGE,

    }

    private static class Message {
        private Boolean left = true;

        public Boolean getLeft() {
            return left;
        }

        public void setLeft(Boolean left) {
            this.left = left;
        }

        private MessageType type = MessageType.SELECTABLE_TEXT;

        public MessageType getType() {
            return type;
        }

        public void setType(MessageType type) {
            this.type = type;
        }

        private StringProperty msg = new SimpleStringProperty();

        public StringProperty msgProperty() {
            return msg;
        }

        public void setMsg(String value) {
            msg.set(value);
        }

        private String imageUrl = "https://qiniu-web-assets.dcloud.net.cn/unidoc/zh/shuijiao.jpg";

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
