package com.icuxika;

import com.icuxika.jextract.win32.HOOKPROC;
import com.icuxika.jextract.win32.KBDLLHOOKSTRUCT;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static com.icuxika.jextract.win32.ffm_h.*;

public class GlobalKeyboardListener {

    private final Stage stage;
    private int currentThreadId;

    private MemorySegment hook = MemorySegment.NULL;
    private boolean ctrlPressed = false; // 162
    private boolean altPressed = false; // 164
    private boolean zPressed = false; // 90

    public GlobalKeyboardListener(Stage stage) {
        this.stage = stage;
    }

    public void hook() {
        new Thread(() -> {
            currentThreadId = GetCurrentThreadId();
            try (Arena arena = Arena.ofConfined()) {
                hook = SetWindowsHookExW(WH_KEYBOARD_LL(), HOOKPROC.allocate((code, wParam, lParam) -> {
                    var kbDllHookStruct = KBDLLHOOKSTRUCT.reinterpret(MemorySegment.ofAddress(lParam), arena, _ -> {
                    });
                    var vkCode = KBDLLHOOKSTRUCT.vkCode(kbDllHookStruct);
                    if (vkCode >= 0) {
                        if (wParam == WM_KEYDOWN()) {
                            System.out.println("按下->" + vkCode);
                            handleKeyDown(vkCode);
                        } else if (wParam == WM_KEYUP()) {
                            System.out.println("松开->" + vkCode);
                            handleKeyUp(vkCode);
                        }
                    }
                    return CallNextHookEx(hook, code, wParam, lParam);
                }, arena), MemorySegment.NULL, 0);
                System.out.println("hook");
                while (GetMessageW(arena.allocate(LPMSG), MemorySegment.NULL, 0, 0) != 0) {
                    System.out.println(1);
                }
                UnhookWindowsHookEx(hook);
                System.out.println("unhook");
            }
        }).start();
    }

    /**
     * 向GetMessageW创建的消息队列发送结束信号，使while循环结束，然后执行卸载全局键盘事件监听钩子
     */
    public void stop() {
        PostThreadMessageW(currentThreadId, WM_QUIT(), 0, 0);
    }

    /**
     * 卸载全局键盘事件监听钩子，但是GetMessageW创建的消息队列不会退出，合理的方式是调用{@link GlobalKeyboardListener#stop()}
     */
    public void unhook() {
        UnhookWindowsHookEx(hook);
    }

    private void handleKeyDown(int vkCode) {
        if (vkCode == 162) {
            ctrlPressed = true;
        } else if (vkCode == 164) {
            altPressed = true;
        } else if (vkCode == 90) {
            zPressed = true;
        }
        if (ctrlPressed && altPressed && zPressed) {
            System.out.println("Ctrl + Alt + Z");
            Platform.runLater(() -> {
                stage.setIconified(!stage.isIconified());
                stage.toFront();
            });
        }
    }

    private void handleKeyUp(int vkCode) {
        if (vkCode == 162) {
            ctrlPressed = false;
        } else if (vkCode == 164) {
            altPressed = false;
        } else if (vkCode == 90) {
            zPressed = false;
        }
    }
}
