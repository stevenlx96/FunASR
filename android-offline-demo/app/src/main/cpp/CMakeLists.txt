cmake_minimum_required(VERSION 3.22.1)
project("funasr_android")

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -Wall -Wno-sign-compare")

# FunASR 源码路径
set(FUNASR_SRC_DIR ${CMAKE_SOURCE_DIR}/funasr)
set(THIRD_PARTY_DIR ${CMAKE_SOURCE_DIR}/third_party)

# 包含头文件
include_directories(
    ${FUNASR_SRC_DIR}/include
    ${FUNASR_SRC_DIR}/src
    ${THIRD_PARTY_DIR}
    ${THIRD_PARTY_DIR}/yaml-cpp/include
    ${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc
)

# 添加 FunASR 核心源文件（排除不需要的文件）
file(GLOB FUNASR_SOURCES
    ${FUNASR_SRC_DIR}/src/funasrruntime.cpp
    ${FUNASR_SRC_DIR}/src/paraformer.cpp
    ${FUNASR_SRC_DIR}/src/model.cpp
    ${FUNASR_SRC_DIR}/src/audio.cpp
    ${FUNASR_SRC_DIR}/src/vocab.cpp
    ${FUNASR_SRC_DIR}/src/util.cpp
    ${FUNASR_SRC_DIR}/src/tokenizer.cpp
    ${FUNASR_SRC_DIR}/src/phone-set.cpp
)

# yaml-cpp 编译
set(YAML_CPP_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(YAML_CPP_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(YAML_CPP_BUILD_CONTRIB OFF CACHE BOOL "" FORCE)
add_subdirectory(${THIRD_PARTY_DIR}/yaml-cpp EXCLUDE_FROM_ALL)

# kaldi-native-fbank 编译
set(KALDI_NATIVE_FBANK_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(KALDI_NATIVE_FBANK_BUILD_PYTHON OFF CACHE BOOL "" FORCE)
add_subdirectory(${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank EXCLUDE_FROM_ALL)

# 创建共享库
add_library(
    funasr_android
    SHARED
    native-lib.cpp
    ${FUNASR_SOURCES}
)

# 链接 ONNX Runtime
set(ONNX_RUNTIME_DIR ${CMAKE_SOURCE_DIR}/../jniLibs)
add_library(onnxruntime SHARED IMPORTED)
set_target_properties(
    onnxruntime
    PROPERTIES IMPORTED_LOCATION
    ${ONNX_RUNTIME_DIR}/${ANDROID_ABI}/libonnxruntime.so
)

# 链接所有库
target_link_libraries(
    funasr_android
    android
    log
    yaml-cpp
    kaldi-native-fbank-core
    onnxruntime
)
