package com.icuxika.model;

import com.icuxika.constant.MessageType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
}
