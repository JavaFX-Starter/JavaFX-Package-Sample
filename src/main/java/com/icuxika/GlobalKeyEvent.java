package com.icuxika;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GlobalKeyEvent {

    private final String id = UUID.randomUUID().toString();

    /**
     * 快捷键
     */
    private final List<KeyCodeStatus> keyCodeList = new ArrayList<>();
    private Runnable action;

    public GlobalKeyEvent combine(int keyCode) {
        this.keyCodeList.add(new KeyCodeStatus(keyCode));
        return this;
    }

    public GlobalKeyEvent onAction(Runnable runnable) {
        this.action = runnable;
        return this;
    }

    public void update(int keyCode, boolean pressed) {
        keyCodeList.stream().filter(k -> k.getKeyCode() == keyCode).findFirst().ifPresent(keyCodeStatus -> keyCodeStatus.setPressed(pressed));
        boolean allKeysPressed = keyCodeList.stream().allMatch(KeyCodeStatus::getPressed);
        if (allKeysPressed) {
            action.run();
        }
    }

    public void print() {
        this.keyCodeList.forEach(keyCodeStatus -> System.out.println(keyCodeStatus.getKeyCode()));
    }

    public String getId() {
        return id;
    }

    private static class KeyCodeStatus {
        private final Integer keyCode;
        private Boolean pressed = false;

        public KeyCodeStatus(Integer keyCode) {
            this.keyCode = keyCode;
        }

        public Integer getKeyCode() {
            return keyCode;
        }

        public Boolean getPressed() {
            return pressed;
        }

        public void setPressed(Boolean pressed) {
            this.pressed = pressed;
        }
    }
}
