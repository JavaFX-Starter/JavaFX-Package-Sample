#pragma once

#include "jni.h"
#include <Windows.h>
#include <memory>
#include <string>

class NativeSingleton {
public:
  NativeSingleton();
  ~NativeSingleton();
  bool IsApplicationRunning(const std::wstring &mutexName);
  std::wstring JStr2WStr(JNIEnv *env, jstring jstr);

private:
  HANDLE mutex;
};

extern std::unique_ptr<NativeSingleton> nativeSingleton;