package com.icuxika.constant;

public enum SubWindow {
    CHAT("chat"),
    CODE_EDITOR("code-editor"),
    MAP("map");

    private String value;

    SubWindow(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
