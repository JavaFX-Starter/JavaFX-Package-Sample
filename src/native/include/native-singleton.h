#pragma once

#include <Windows.h>
#include <memory>
#include <string>

class NativeSingleton {
public:
  NativeSingleton();
  ~NativeSingleton();
  bool IsApplicationRunning(const std::wstring &mutexName);

private:
  HANDLE mutex;
};

extern std::unique_ptr<NativeSingleton> nativeSingleton;