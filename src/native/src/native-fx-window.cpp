#include "com_icuxika_jni_NativeFXWindow.h"
#include "jni.h"
#include "jni_md.h"
#include "native-singleton.h"
#include "native-util.h"
#include "native-window-proc.h"
#include <Windows.h>
#include <cstddef>
#include <memory>
#include <string>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_icuxika_jni_NativeFXWindow_getHWnd(
    JNIEnv *env, jclass clazz, jobject stage) {
  if (stage == nullptr) {
    return 0L;
  }
  jclass stageClass = env->FindClass("javafx/stage/Window");
  jmethodID getPeerMethod =
      env->GetMethodID(stageClass, "getPeer", "()Lcom/sun/javafx/tk/TKStage;");
  jclass tkStageClass = env->FindClass("com/sun/javafx/tk/TKStage");
  jmethodID getRawHandleMethod =
      env->GetMethodID(tkStageClass, "getRawHandle", "()J");
  jobject tkStageObject = env->CallObjectMethod(stage, getPeerMethod);
  jlong hWnd = env->CallLongMethod(tkStageObject, getRawHandleMethod);
  return hWnd;
}

JNIEXPORT jstring JNICALL Java_com_icuxika_jni_NativeFXWindow_getWindowText(
    JNIEnv *env, jclass clazz, jlong hWnd) {
  int windowTextLength = GetWindowTextLength(reinterpret_cast<HWND>(hWnd));
  if (windowTextLength > 0) {
    std::wstring buffer(windowTextLength + 1, L'\0');
    if (GetWindowText(reinterpret_cast<HWND>(hWnd), &buffer[0],
                      windowTextLength + 1) > 0) {
      return NativeUtil::wstr2jstr(env, buffer);
    }
  }
  return nullptr;
}

JNIEXPORT jstring JNICALL Java_com_icuxika_jni_NativeFXWindow_getClassName(
    JNIEnv *env, jclass clazz, jlong hWnd) {
  int length = 256;
  std::wstring buffer(length, L'\0');
  int actualLen =
      GetClassName(reinterpret_cast<HWND>(hWnd), &buffer[0], length);
  if (actualLen > 0) {
    buffer.resize(actualLen);
    return NativeUtil::wstr2jstr(env, buffer);
  }
  return nullptr;
}

JNIEXPORT jboolean JNICALL Java_com_icuxika_jni_NativeFXWindow_registerHotKey(
    JNIEnv *env, jclass clazz, jlong hWnd, jint id, jint fsModifiers, jint vk) {
  if (RegisterHotKey(reinterpret_cast<HWND>(hWnd), id, fsModifiers, vk)) {
    return JNI_TRUE;
  }
  return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_icuxika_jni_NativeFXWindow_unregisterHotKey(
    JNIEnv *env, jclass clazz, jlong hWnd, jint id) {
  if (UnregisterHotKey(reinterpret_cast<HWND>(hWnd), id)) {
    return JNI_TRUE;
  }
  return JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_icuxika_jni_NativeFXWindow_setWindowTransparency(JNIEnv *env,
                                                          jclass clazz,
                                                          jlong hWnd) {
  SetWindowLong(reinterpret_cast<HWND>(hWnd), GWL_EXSTYLE,
                GetWindowLong(reinterpret_cast<HWND>(hWnd), GWL_EXSTYLE) |
                    WS_EX_LAYERED);
  SetLayeredWindowAttributes(reinterpret_cast<HWND>(hWnd), 0, (255 * 70) / 100,
                             LWA_ALPHA);
}

JNIEXPORT void JNICALL
Java_com_icuxika_jni_NativeFXWindow_unsetWindowTransparency(JNIEnv *env,
                                                            jclass clazz,
                                                            jlong hWnd) {
  SetWindowLong(reinterpret_cast<HWND>(hWnd), GWL_EXSTYLE,
                GetWindowLong(reinterpret_cast<HWND>(hWnd), GWL_EXSTYLE) &
                    ~WS_EX_LAYERED);
  RedrawWindow(reinterpret_cast<HWND>(hWnd), NULL, NULL,
               RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN);
}

JNIEXPORT void JNICALL Java_com_icuxika_jni_NativeFXWindow_initialize(
    JNIEnv *env, jobject obj, jlong hWnd) {
  nativeWindowProc = std::make_unique<NativeWindowProc>(env, obj, hWnd);
  nativeWindowProc->defaultWndProc =
      (WNDPROC)SetWindowLongPtr(reinterpret_cast<HWND>(hWnd), GWLP_WNDPROC,
                                (LONG_PTR)NativeWindowProc::StaticWndProc);
}

JNIEXPORT jboolean JNICALL
Java_com_icuxika_jni_NativeFXWindow_isApplicationRunning(JNIEnv *env,
                                                         jclass clazz,
                                                         jstring mutexName) {
  nativeSingleton = std::make_unique<NativeSingleton>();
  std::wstring m = NativeUtil::jstr2wstr(env, mutexName);
  return nativeSingleton->IsApplicationRunning(m);
}

JNIEXPORT void JNICALL Java_com_icuxika_jni_NativeFXWindow_callPrevInstance(
    JNIEnv *env, jclass clazz, jstring message, jstring className,
    jstring windowName) {
  std::wstring m = NativeUtil::jstr2wstr(env, message);
  std::wstring c = NativeUtil::jstr2wstr(env, className);
  std::wstring w = NativeUtil::jstr2wstr(env, windowName);

  if (const HWND hWnd = FindWindow(c.c_str(), w.c_str())) {
    ShowWindow(hWnd, SW_RESTORE);
    SetForegroundWindow(hWnd);

    const std::wstring &msg = m;

    COPYDATASTRUCT cds;
    cds.dwData = 1;
    cds.cbData = (msg.size() + 1) * sizeof(wchar_t);
    cds.lpData = (void *)msg.c_str();

    SendMessage(hWnd, WM_COPYDATA, 0, reinterpret_cast<LPARAM>(&cds));
  }
}
#ifdef __cplusplus
}
#endif