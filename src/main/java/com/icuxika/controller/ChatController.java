package com.icuxika.controller;

import com.icuxika.AppResource;
import com.icuxika.FXUtil;
import com.icuxika.cell.ChatMessageCell;
import com.icuxika.constant.MessageSendType;
import com.icuxika.constant.MessageType;
import com.icuxika.model.ChatMessage;
import com.icuxika.richtext.ChatInputItem;
import com.icuxika.richtext.ChatInputTextArea;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jfx.incubator.scene.control.richtext.SelectionSegment;
import jfx.incubator.scene.control.richtext.TextPos;
import org.kordamp.ikonli.fontawesome6.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class ChatController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);
    @FXML
    private BorderPane rootContainer;
    @FXML
    private BorderPane contentContainer;

    private final ListView<ChatMessage> messageListView = new ListView<>();
    private final ObservableList<ChatMessage> chatMessageObservableList = FXCollections.observableArrayList();

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
        messageListView.setItems(chatMessageObservableList);
        messageListView.setCellFactory(_ -> new ChatMessageCell());
        contentContainer.setCenter(messageListView);

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
        ChatMessage imageMsg1 = new ChatMessage();
        imageMsg1.setType(MessageType.IMAGE);
        chatMessageObservableList.add(imageMsg1);

        ChatMessage msg1 = new ChatMessage();
        msg1.setLeft(true);
        msg1.setMsg("你要好好长大，不要输给风，不要输给雨，不要输给冬雪，不要输给炎夏。少年人，你在孩童时应当快乐，使你的心欢畅，行你所愿行的，见你所愿见的，然而也应当记住黑暗的时日。但愿新的梦想永远不被无留陀侵蚀，但愿旧的故事与无留陀一同被忘却，但愿绿色的原野，山丘永远不会变得枯黄，但愿溪水永远清澈，但愿鲜花永远盛开。挚友将再次同行于茂密的森林中，一切美好的事物终将归来，一切痛苦的记忆也会远去，就像溪水净化自己，枯树绽出新芽。最终，森林会记住一切。");
        chatMessageObservableList.add(msg1);

        ChatMessage msg2 = new ChatMessage();
        msg2.setLeft(false);
        msg2.setMsg("如此绚丽的花朵，不该在绽放之前就枯萎。我会赠予你璀璨的祝福，而你的灵魂，也将绽放更耀眼的光辉。亲爱的山雀，请将我的箭，我的花，与我的爱，带给那孑然独行的旅人。愿你前行的道路有群星闪耀。愿你留下的足迹有百花绽放。你即是上帝的馈赠，世界因你而瑰丽。");
        chatMessageObservableList.add(msg2);

        ChatMessage msg3 = new ChatMessage();
        msg3.setLeft(true);
        msg3.setMsg("你要好好长大，不要输给风，不要输给雨，不要输给冬雪，不要输给炎夏。少年人，你在孩童时应当快乐，使你的心欢畅，行你所愿行的，见你所愿见的，然而也应当记住黑暗的时日。但愿新的梦想永远不被无留陀侵蚀，但愿旧的故事与无留陀一同被忘却，但愿绿色的原野，山丘永远不会变得枯黄，但愿溪水永远清澈，但愿鲜花永远盛开。挚友将再次同行于茂密的森林中，一切美好的事物终将归来，一切痛苦的记忆也会远去，就像溪水净化自己，枯树绽出新芽。最终，森林会记住一切。");
        chatMessageObservableList.add(msg3);

        ChatMessage msg4 = new ChatMessage();
        msg4.setLeft(false);
        msg4.setMsg("如此绚丽的花朵，不该在绽放之前就枯萎。我会赠予你璀璨的祝福，而你的灵魂，也将绽放更耀眼的光辉。亲爱的山雀，请将我的箭，我的花，与我的爱，带给那孑然独行的旅人。愿你前行的道路有群星闪耀。愿你留下的足迹有百花绽放。你即是上帝的馈赠，世界因你而瑰丽。");
        chatMessageObservableList.add(msg4);

        ChatMessage imageMsg2 = new ChatMessage();
        imageMsg2.setType(MessageType.IMAGE);
        imageMsg2.setImageUrl("https://www.baidu.com/baidu.html");
        chatMessageObservableList.add(imageMsg2);

        ChatMessage imageMsg3 = new ChatMessage();
        imageMsg3.setType(MessageType.IMAGE);
        imageMsg3.setImageUrl("https://i.pixiv.re/img-master/img/2026/03/11/00/02/18/142151329_p0_master1200.jpg");
        chatMessageObservableList.add(imageMsg3);

        Thread.ofVirtual().start(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(AppResource.class.getResourceAsStream("/chat_mock.txt"))))) {
                reader.lines().forEach(line -> {
                    ChatMessage m = new ChatMessage();
                    m.setLeft(ThreadLocalRandom.current().nextBoolean());
                    m.setMsg(line);
                    FXUtil.awaitPulse();
                    Platform.runLater(() -> chatMessageObservableList.add(m));
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void sendMessage() {
        List<ChatInputItem> items = chatInputTextArea.getChatInputItems();
        int index = 1;
        for (ChatInputItem item : items) {
            if (item instanceof ChatInputItem.Text(String text)) {
                System.out.println("文本==========");
                System.out.println(text);
                ChatMessage msg = new ChatMessage();
                msg.setLeft(false);
                msg.setMsg(text.trim());
                chatMessageObservableList.add(msg);
                System.out.println("文本==========");
                // 发送文本消息...
            } else if (item instanceof ChatInputItem.ImageItem(Image image)) {
                System.out.println("图片: " + image);
                // 上传图片并发送...
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                try {
                    File file = new File("target\\" + index + ".png");
                    ImageIO.write(bufferedImage, "png", file);

                    ChatMessage msg = new ChatMessage();
                    msg.setLeft(false);
                    msg.setType(MessageType.IMAGE);
                    msg.setImageUrl("file:/" + file.getAbsolutePath());
                    chatMessageObservableList.add(msg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                index++;
            }
        }
        messageListView.scrollTo(chatMessageObservableList.size() - 1);
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

}
