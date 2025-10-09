#include "native-util.h"

std::wstring NativeUtil::jstr2wstr(JNIEnv *env, jstring jstr) {
  if (jstr == nullptr) {
    return L"";
  }
  const jchar *chars = env->GetStringChars(jstr, nullptr);
  jsize len = env->GetStringLength(jstr);
  std::wstring result((const wchar_t *)chars, len);
  env->ReleaseStringChars(jstr, chars);
  return result;
}

jstring NativeUtil::wstr2jstr(JNIEnv *env, const std::wstring &wstr) {
  return env->NewString((const jchar *)wstr.c_str(), wstr.length());
}