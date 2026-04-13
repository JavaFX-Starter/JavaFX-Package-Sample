package com.icuxika.model;

import com.icuxika.constant.MessageType;
import javafx.beans.property.*;
import javafx.scene.image.Image;

public class ChatMessage {
    private String avatar = "https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg";

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

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

    private final StringProperty msg = new SimpleStringProperty();

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

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    private final DoubleProperty fitWidth = new SimpleDoubleProperty();

    public DoubleProperty fitWidthProperty() {
        return fitWidth;
    }

    private final DoubleProperty imageProgress = new SimpleDoubleProperty();

    public DoubleProperty imageProgressProperty() {
        return imageProgress;
    }

    private final BooleanProperty imageProgressVisible = new SimpleBooleanProperty(true);

    public BooleanProperty imageProgressVisibleProperty() {
        return imageProgressVisible;
    }

    private final StringProperty imageErrorText = new SimpleStringProperty("");

    public StringProperty imageErrorTextProperty() {
        return imageErrorText;
    }

    private final BooleanProperty imageErrorVisible = new SimpleBooleanProperty(false);

    public BooleanProperty imageErrorVisibleProperty() {
        return imageErrorVisible;
    }
}
