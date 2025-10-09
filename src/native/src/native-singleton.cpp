#include "native-singleton.h"
#include <string>

NativeSingleton::NativeSingleton() {}
NativeSingleton::~NativeSingleton() {
  if (mutex) {
    CloseHandle(mutex);
  }
}

bool NativeSingleton::IsApplicationRunning(const std::wstring &mutexName) {
  mutex = CreateMutex(nullptr, false, mutexName.c_str());
  if (mutex != nullptr) {
    if (GetLastError() == ERROR_ALREADY_EXISTS) {
      CloseHandle(mutex);
      mutex = nullptr;
      return true;
    }
    return false;
  }
  // 这里暂不判断CreateMutex创建失败的情况
  return false;
}

std::wstring NativeSingleton::JStr2WStr(JNIEnv *env, jstring jstr) {
  if (jstr == nullptr) {
    return L"";
  }
  const char *chars = env->GetStringUTFChars(jstr, nullptr);
  if (chars == nullptr) {
    return L"";
  }
  int len = MultiByteToWideChar(CP_UTF8, 0, chars, -1, nullptr, 0);
  if (len == 0) {
    env->ReleaseStringUTFChars(jstr, chars);
    return L"";
  }
  std::wstring wstr(len - 1, 0);
  MultiByteToWideChar(CP_UTF8, 0, chars, -1, &wstr[0], len);
  env->ReleaseStringUTFChars(jstr, chars);
  return wstr;
}

std::unique_ptr<NativeSingleton> nativeSingleton;