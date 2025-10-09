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

std::unique_ptr<NativeSingleton> nativeSingleton;