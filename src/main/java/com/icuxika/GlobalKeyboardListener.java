package com.icuxika;

import com.icuxika.jextract.win32.HOOKPROC;
import com.icuxika.jextract.win32.KBDLLHOOKSTRUCT;
import com.icuxika.jextract.win32.tagMSG;
import com.icuxika.jni.NativeFXWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.ConcurrentHashMap;

import static com.icuxika.jextract.win32.ffm_h.*;

public class GlobalKeyboardListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalKeyboardListener.class);

    public static ConcurrentHashMap<String, GlobalKeyEvent> globalKeyEventMap = new ConcurrentHashMap<>();

    public static void registerGlobalKeyEvent(GlobalKeyEvent globalKeyEvent) {
        globalKeyEventMap.put(globalKeyEvent.getId(), globalKeyEvent);
    }

    public static void unregisterGlobalKeyEvent(String id) {
        globalKeyEventMap.remove(id);
    }

    private Runnable callback;

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }

    public Runnable getCallback() {
        return callback;
    }

    private int currentThreadId;
    private MemorySegment hook = MemorySegment.NULL;

    public void hook() {
        new Thread(() -> {
            currentThreadId = GetCurrentThreadId();
            try (Arena arena = Arena.ofConfined()) {

                // Ctrl + Alt + A
                // 说明此线程的消息循环与JavaFX主线程的消息循环不是同一个，且也无法处理JavaFX主线程WM_HOTKEY消息
                // 因此这里调用RegisterHotKey没有传入hWnd参数，而hWnd参数为NULL时，在这个线程上RegisterHotKey的快捷键消息只能被这个线程的消息循环所处理
                // 如果需要实现已有其他程序RegisterHotKey注册了相同快捷键时就不注册、修改等逻辑，应该重新设计这个线程的逻辑
                // 每次更改，取消注册所有的快捷键，然后结束消息循环，重新创建相应的线程
                // 也可以将RegisterHotKey只用来检测是否有其他程序已经注册了快捷键，然后本应用的全局快捷键功能全部由SetWindowsHookExW实现
                // 这样在其他线程也可以调用RegisterHotKey了，此类功能逻辑也不需要变动
                // TODO 沙盒中对于 SetWindowsHookExW 方式设置的快捷键不够敏感
                boolean success = NativeFXWindow.registerHotKey(1, 0x0002 | 0x0001, 0x41);
                if (!success) {
                    LOGGER.error("[RegisterHotKey]注册快捷键失败");
                }

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
                LOGGER.info("全局键盘事件钩子已安装");
                //noinspection StatementWithEmptyBody
                MemorySegment msg = arena.allocate(LPMSG);
                while (GetMessageW(msg, MemorySegment.NULL, 0, 0) != 0) {
                    var m = tagMSG.reinterpret(msg, arena, _ -> {
                    });
                    if (tagMSG.message(m) == WM_HOTKEY()) {
                        var id = tagMSG.wParam(m);
                        getCallback().run();
                        LOGGER.info("使用[RegisterHotKey]注册的快捷键[{}]被触发了", id);
                    }
                    TranslateMessage(msg);
                    DispatchMessageW(msg);
                }
                UnhookWindowsHookEx(hook);
                LOGGER.info("全局键盘事件钩子已卸载");

                NativeFXWindow.unregisterHotKey(1);
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
     * 调用此函数会导致程序运行时崩溃
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
