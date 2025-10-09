#include "native-window-proc.h"
#include "jni.h"
#include <Windows.h>
#include <iostream>

NativeWindowProc::NativeWindowProc(JNIEnv *env, jobject obj, jlong hWnd)
    : _env(env), _obj(obj) {
  _env->GetJavaVM(&_javaVM);
  _objRef = _env->NewGlobalRef(_obj);
  _clazz = _env->GetObjectClass(_obj);
  callbackHotKeyMethodID = _env->GetMethodID(_clazz, "callbackHotKey", "(I)V");
  callPrevInstanceMethodID =
      _env->GetMethodID(_clazz, "callPrevInstance", "(Ljava/lang/String;)V");
}

NativeWindowProc::~NativeWindowProc() {}

LRESULT CALLBACK NativeWindowProc::MainWndProc(HWND hWnd, UINT uMsg,
                                               WPARAM wParam, LPARAM lParam) {
  switch (uMsg) {
  case WM_COMMAND:
    switch (LOWORD(wParam)) {}
    break;
  case WM_HOTKEY: {
    const int id = static_cast<int>(wParam);
    std::cout << id << std::endl;
    if (id == 1) {
      ShowWindow(hWnd, SW_RESTORE);
      SetForegroundWindow(hWnd);
    }

    boolean detached = false;
    GetOperateEnv(detached);
    _operateEnv->CallVoidMethod(_objRef, callbackHotKeyMethodID, id);
    if (detached) {
      _javaVM->DetachCurrentThread();
    }
  } break;
  case WM_COPYDATA: {
    PCOPYDATASTRUCT pCDS = reinterpret_cast<PCOPYDATASTRUCT>(lParam);
    switch (pCDS->dwData) {
    case 1: {
      std::wstring received(static_cast<wchar_t *>(pCDS->lpData),
                            pCDS->cbData / sizeof(wchar_t));
      copyMessage = received;
      PostMessage(hWnd, WM_APP + 1, 0, 0);
      break;
    }
    default:;
    }
    break;
  }
  case WM_APP + 1: {
    ShowWindow(hWnd, SW_RESTORE);
    SetForegroundWindow(hWnd);
    MessageBox(hWnd, copyMessage.c_str(), L"浏览器唤起", MB_OK);
    boolean detached = false;
    GetOperateEnv(detached);

    int sizeNeeded = WideCharToMultiByte(CP_UTF8, 0, copyMessage.c_str(), -1,
                                         nullptr, 0, nullptr, nullptr);
    std::string utf8Str(sizeNeeded, '\0');
    WideCharToMultiByte(CP_UTF8, 0, copyMessage.c_str(), -1, &utf8Str[0],
                        sizeNeeded, nullptr, nullptr);

    jstring jStr = _operateEnv->NewStringUTF(utf8Str.c_str());
    _operateEnv->CallVoidMethod(_objRef, callPrevInstanceMethodID, jStr);
    if (detached) {
      _javaVM->DetachCurrentThread();
    }
    break;
  }
  default:
    return CallWindowProc(nativeWindowProc->defaultWndProc, hWnd, uMsg, wParam,
                          lParam);
  }
  return CallWindowProc(nativeWindowProc->defaultWndProc, hWnd, uMsg, wParam,
                        lParam);
}

LRESULT CALLBACK NativeWindowProc::StaticWndProc(HWND hWnd, UINT uMsg,
                                                 WPARAM wParam, LPARAM lParam) {
  return nativeWindowProc->MainWndProc(hWnd, uMsg, wParam, lParam);
}

void NativeWindowProc::GetOperateEnv(boolean &detached) {
  int result = _javaVM->GetEnv((void **)&_operateEnv, JNI_VERSION_1_8);
  if (result == JNI_EDETACHED) {
    _javaVM->AttachCurrentThread((void **)&_operateEnv, nullptr);
    detached = true;
  }
}

std::unique_ptr<NativeWindowProc> nativeWindowProc;