package com.icuxika.richtext;

import javafx.scene.image.Image;

public sealed interface ChatInputItem permits ChatInputItem.Text, ChatInputItem.ImageItem {
    record Text(String text) implements ChatInputItem {
    }

    record ImageItem(Image image) implements ChatInputItem {
    }
}
