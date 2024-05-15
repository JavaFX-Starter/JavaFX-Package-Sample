package com.icuxika;

import com.icuxika.jextract.win32.HOOKPROC;
import com.icuxika.jextract.win32.KBDLLHOOKSTRUCT;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.ConcurrentHashMap;

import static com.icuxika.jextract.win32.ffm_h.*;

public class GlobalKeyboardListener {

    public static ConcurrentHashMap<String, GlobalKeyEvent> globalKeyEventMap = new ConcurrentHashMap<>();

    public static void registerGlobalKeyEvent(GlobalKeyEvent globalKeyEvent) {
        globalKeyEventMap.put(globalKeyEvent.getId(), globalKeyEvent);
    }

    public static void unregisterGlobalKeyEvent(String id) {
        globalKeyEventMap.remove(id);
    }

    private int currentThreadId;
    private MemorySegment hook = MemorySegment.NULL;

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
        globalKeyEventMap.values().forEach(globalKeyEvent -> globalKeyEvent.update(vkCode, true));
    }

    private void handleKeyUp(int vkCode) {
        globalKeyEventMap.values().forEach(globalKeyEvent -> globalKeyEvent.update(vkCode, false));
    }
}
