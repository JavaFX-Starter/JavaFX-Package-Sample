#pragma once

#include "jni.h"
#include <Windows.h>
#include <string>

class NativeUtil {
public:
  static std::wstring jstr2wstr(JNIEnv *env, jstring jstr);

  static jstring wstr2jstr(JNIEnv *env, const std::wstring &wstr);
};