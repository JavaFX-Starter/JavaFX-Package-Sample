#include "com_icuxika_jni_NativeFXWindow.h"
#include "jni.h"
#include "jni_md.h"
#include "native-singleton.h"
#include "native-util.h"
#include "native-window-proc.h"
#include <Windows.h>
#include <cmath>
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

JNIEXPORT jstring JNICALL Java_com_icuxika_jni_NativeFXWindow_getWindowName(
    JNIEnv *env, jclass clazz, jlong hWnd) {
  int windowTextLength = GetWindowTextLength(reinterpret_cast<HWND>(hWnd));
  if (windowTextLength > 0) {
    std::wstring buffer(windowTextLength + 1, L'\0');
    if (GetWindowText(reinterpret_cast<HWND>(hWnd), &buffer[0],
                      windowTextLength + 1) > 0) {
      buffer.resize(windowTextLength);
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
Java_com_icuxika_jni_NativeFXWindow_setWindowTransparencyAlpha(JNIEnv *env,
                                                               jclass clazz,
                                                               jlong hWnd,
                                                               jint alpha) {
  SetWindowLong(reinterpret_cast<HWND>(hWnd), GWL_EXSTYLE,
                GetWindowLong(reinterpret_cast<HWND>(hWnd), GWL_EXSTYLE) |
                    WS_EX_LAYERED);
  SetLayeredWindowAttributes(reinterpret_cast<HWND>(hWnd), 0,
                             (255 * alpha) / 100, LWA_ALPHA);
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

JNIEXPORT void JNICALL Java_com_icuxika_jni_NativeFXWindow_runAsAdmin(
    JNIEnv *env, jclass clazz, jstring exePath, jstring parameters,
    jstring workingDir, jboolean waitForExit) {
  std::wstring e = NativeUtil::jstr2wstr(env, exePath);
  std::wstring p = NativeUtil::jstr2wstr(env, parameters);
  std::wstring w = NativeUtil::jstr2wstr(env, workingDir);

  SHELLEXECUTEINFO shellExecuteInfo = {};
  shellExecuteInfo.cbSize = sizeof(shellExecuteInfo);
  shellExecuteInfo.fMask =
      waitForExit ? SEE_MASK_NOCLOSEPROCESS : SEE_MASK_DEFAULT;
  shellExecuteInfo.hwnd = nullptr;
  shellExecuteInfo.lpVerb = L"runas";
  shellExecuteInfo.lpFile = e.c_str();
  shellExecuteInfo.lpParameters = p.c_str();
  shellExecuteInfo.lpDirectory = w.c_str();
  shellExecuteInfo.nShow = SW_SHOWNORMAL;
  if (ShellExecuteEx(&shellExecuteInfo)) {
    if (waitForExit) {
      if (shellExecuteInfo.hProcess) {
        WaitForSingleObject(shellExecuteInfo.hProcess, INFINITY);
        CloseHandle(shellExecuteInfo.hProcess);
      }
    }
  }
}

JNIEXPORT jstring JNICALL Java_com_icuxika_jni_NativeFXWindow_getExecutablePath(
    JNIEnv *env, jclass clazz) {
  wchar_t exePath[MAX_PATH];
  GetModuleFileName(nullptr, exePath, MAX_PATH);
  const std::wstring currentPath = exePath;
  return NativeUtil::wstr2jstr(env, exePath);
}

JNIEXPORT jboolean JNICALL Java_com_icuxika_jni_NativeFXWindow_isStartupEnable(
    JNIEnv *env, jclass clazz, jstring windowName) {
  HKEY hKey = nullptr;
  LONG result = RegOpenKeyEx(
      HKEY_CURRENT_USER, L"Software\\Microsoft\\Windows\\CurrentVersion\\Run",
      0, KEY_READ, &hKey);
  if (result != ERROR_SUCCESS) {
    return false;
  }

  DWORD dwType = REG_SZ;
  wchar_t regValue[MAX_PATH];
  DWORD size = sizeof(regValue);
  result = RegQueryValueEx(hKey, NativeUtil::jstr2wstr(env, windowName).data(),
                           nullptr, &dwType, reinterpret_cast<LPBYTE>(regValue),
                           &size);
  RegCloseKey(hKey);
  if (result != ERROR_SUCCESS) {
    return false;
  }

  wchar_t exePath[MAX_PATH];
  GetModuleFileName(nullptr, exePath, MAX_PATH);

  std::wstring regPath = regValue;
  const std::wstring currentPath = exePath;

  if (!regPath.empty() && regPath[0] == L'\"') {
    const size_t endQuote = regPath.find(L'\"', 1);
    if (endQuote != std::wstring::npos) {
      regPath = regPath.substr(1, endQuote - 1);
    }
  }

  return _wcsicmp(regPath.c_str(), currentPath.c_str()) == 0;
}

JNIEXPORT jboolean JNICALL Java_com_icuxika_jni_NativeFXWindow_setStartup(
    JNIEnv *env, jclass clazz, jstring windowName, jboolean enable) {
  wchar_t path[MAX_PATH];
  GetModuleFileName(nullptr, path, MAX_PATH);
  HKEY hKey = nullptr;
  LONG result = RegOpenKeyEx(
      HKEY_CURRENT_USER, L"Software\\Microsoft\\Windows\\CurrentVersion\\Run",
      0, KEY_WRITE, &hKey);
  if (result != ERROR_SUCCESS) {
    return false;
  }
  if (enable) {
    const std::wstring exePath = L"\"" + std::wstring(path) + L"\"";
    result =
        RegSetValueEx(hKey, NativeUtil::jstr2wstr(env, windowName).data(), 0,
                      REG_SZ, reinterpret_cast<const BYTE *>(exePath.c_str()),
                      (exePath.size() + 1) * sizeof(wchar_t));
  } else {
    result =
        RegDeleteValue(hKey, NativeUtil::jstr2wstr(env, windowName).data());
    if (result == ERROR_FILE_NOT_FOUND) {
      result = ERROR_SUCCESS;
    }
  }
  RegCloseKey(hKey);
  return result == ERROR_SUCCESS;
}

#ifdef __cplusplus
}
#endif