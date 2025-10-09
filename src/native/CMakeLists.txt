cmake_minimum_required(VERSION 3.15)
project(NativeFX)
set(CMAKE_CXX_STANDARD 20)

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(CMAKE_CXX_FLAGS "/utf-8 /EHsc")

find_package(JNI REQUIRED)
include_directories(${JAVA_INCLUDE_PATH})
include_directories(${JAVA_INCLUDE_PATH2})

set(LIB_NAME NativeFXWindow)
add_library(${LIB_NAME} SHARED src/native-fx-window.cpp src/native-window-proc.cpp src/native-singleton.cpp)
target_include_directories(${LIB_NAME} PUBLIC include)

