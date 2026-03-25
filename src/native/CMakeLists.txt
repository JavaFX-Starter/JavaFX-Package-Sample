cmake_minimum_required(VERSION 3.15)

set(NATIVE_VERSION "0.0.0" CACHE STRING "Project Version")

project(NativeFX VERSION ${NATIVE_VERSION})
set(CMAKE_CXX_STANDARD 20)

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(CMAKE_CXX_FLAGS "/utf-8 /EHsc")

find_package(JNI REQUIRED)
include_directories(${JAVA_INCLUDE_PATH})
include_directories(${JAVA_INCLUDE_PATH2})

set(LIB_NAME NativeFXWindow)
add_library(${LIB_NAME} SHARED src/native-fx-window.cpp src/native-window-proc.cpp src/native-singleton.cpp src/native-util.cpp)
target_include_directories(${LIB_NAME} PUBLIC include)
target_compile_definitions(${LIB_NAME} PRIVATE UNICODE _UNICODE)

set_target_properties(${LIB_NAME} PROPERTIES OUTPUT_NAME "${LIB_NAME}-${PROJECT_VERSION}" VERSION ${PROJECT_VERSION} SOVERSION ${PROJECT_VERSION_MAJOR})