cmake_minimum_required(VERSION 3.31)
project(auto-update-helper)

set(CMAKE_CXX_STANDARD 23)

set(CMAKE_CXX_FLAGS "/utf-8 /EHsc")

add_executable(auto-update-helper main.cpp)
target_link_libraries(auto-update-helper PRIVATE ComCtl32)
target_compile_definitions(auto-update-helper PRIVATE UNICODE _UNICODE)

target_link_options(auto-update-helper PRIVATE 
    $<$<CONFIG:Debug>:/SUBSYSTEM:CONSOLE>
    $<$<NOT:$<CONFIG:Debug>>:/SUBSYSTEM:WINDOWS /ENTRY:mainCRTStartup>
)