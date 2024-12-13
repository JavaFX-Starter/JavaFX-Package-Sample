#include "com_icuxika_jni_NativeFXWindow.h"
#include "jni.h"
#include "jni_md.h"
#include <Windows.h>
#include <cstddef>
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
    if (GetWindowTextW(reinterpret_cast<HWND>(hWnd), &buffer[0],
                       windowTextLength + 1) > 0) {
      int sizeNeeded = WideCharToMultiByte(CP_UTF8, 0, buffer.c_str(), -1,
                                           nullptr, 0, nullptr, nullptr);
      std::string utf8Str(sizeNeeded, '\0');
      WideCharToMultiByte(CP_UTF8, 0, buffer.c_str(), -1, &utf8Str[0],
                          sizeNeeded, nullptr, nullptr);
      return env->NewStringUTF(utf8Str.c_str());
    }
  }
  return nullptr;
}

JNIEXPORT jstring JNICALL Java_com_icuxika_jni_NativeFXWindow_getClassName(
    JNIEnv *env, jclass clazz, jlong hWnd) {
  int length = 256;
  std::wstring buffer(length, L'\0');
  if (GetClassNameW(reinterpret_cast<HWND>(hWnd), &buffer[0], length)) {
    int sizeNeeded = WideCharToMultiByte(CP_UTF8, 0, buffer.c_str(), -1,
                                         nullptr, 0, nullptr, nullptr);
    std::string utf8Str(sizeNeeded, '\0');
    WideCharToMultiByte(CP_UTF8, 0, buffer.c_str(), -1, &utf8Str[0], sizeNeeded,
                        nullptr, nullptr);
    return env->NewStringUTF(utf8Str.c_str());
  }
  return nullptr;
}

JNIEXPORT jboolean JNICALL Java_com_icuxika_jni_NativeFXWindow_registerHotKey(
    JNIEnv *env, jclass clazz, jint id, jint fsModifiers, jint vk) {
  if (RegisterHotKey(NULL, id, fsModifiers, vk)) {
    return JNI_TRUE;
  }
  return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_icuxika_jni_NativeFXWindow_unregisterHotKey(
    JNIEnv *env, jclass clazz, jint id) {
  if (UnregisterHotKey(NULL, id)) {
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

#ifdef __cplusplus
}
#endif