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