cmake_minimum_required(VERSION 3.15)
project(NativeFX)
set(CMAKE_CXX_STANDARD 20)

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

find_package(JNI REQUIRED)
include_directories(${JAVA_INCLUDE_PATH})
include_directories(${JAVA_INCLUDE_PATH2})

set(LIB_NAME NativeFXWindow)
add_library(${LIB_NAME} SHARED src/native-fx-window.cpp)
target_include_directories(${LIB_NAME} PUBLIC include)

