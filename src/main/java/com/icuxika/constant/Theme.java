package com.icuxika.constant;

public enum Theme {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    private String value;

    Theme(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
