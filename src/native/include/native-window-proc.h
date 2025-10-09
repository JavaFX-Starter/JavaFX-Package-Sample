#pragma once

#include "jni.h"
#include <Windows.h>
#include <memory>

class NativeWindowProc {
public:
  NativeWindowProc(JNIEnv *env, jobject obj, jlong hWnd);
  ~NativeWindowProc();

  WNDPROC defaultWndProc;
  LRESULT CALLBACK MainWndProc(HWND hWnd, UINT uMsg, WPARAM wParam,
                               LPARAM lParam);
  static LRESULT CALLBACK StaticWndProc(HWND hWnd, UINT uMsg, WPARAM wParam,
                                        LPARAM lParam);

private:
  JNIEnv *_env;
  jobject _obj;

  JavaVM *_javaVM;
  jobject _objRef;
  jclass _clazz;

  jmethodID callbackHotKeyMethodID;
  JNIEnv *_operateEnv;

  void GetOperateEnv(boolean &detached);
};
extern std::unique_ptr<NativeWindowProc> nativeWindowProc;