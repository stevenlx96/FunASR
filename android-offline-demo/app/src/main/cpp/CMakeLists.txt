cmake_minimum_required(VERSION 3.22.1)
project("funasr_android")

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -Wall")

# ============================================
# 第一阶段：基础JNI库（不依赖FunASR）
# ============================================
# 这个版本可以直接编译运行，用于测试Android项目框架

add_library(
        funasr_android
        SHARED
        native-lib.cpp
)

target_link_libraries(
        funasr_android
        android
        log
)

# ============================================
# 第二阶段：集成FunASR（需要复制源码后启用）
# ============================================
# 取消注释以下代码以集成FunASR C++核心库

## 1. 设置FunASR源码路径
# set(FUNASR_SRC_DIR ${CMAKE_SOURCE_DIR}/funasr)
# set(THIRD_PARTY_DIR ${CMAKE_SOURCE_DIR}/third_party)

## 2. 包含头文件
# include_directories(
#     ${FUNASR_SRC_DIR}/include
#     ${THIRD_PARTY_DIR}
#     ${THIRD_PARTY_DIR}/yaml-cpp/include
#     ${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc
#     ${THIRD_PARTY_DIR}/json/include
# )

## 3. 添加FunASR核心源文件
# file(GLOB FUNASR_SOURCES
#     ${FUNASR_SRC_DIR}/src/*.cpp
# )

## 4. 添加第三方库
# add_subdirectory(${THIRD_PARTY_DIR}/yaml-cpp)
# add_subdirectory(${THIRD_PARTY_DIR}/kaldi-native-fbank)

## 5. 修改共享库配置（替换上面的add_library）
# add_library(
#     funasr_android
#     SHARED
#     native-lib.cpp
#     ${FUNASR_SOURCES}
# )

## 6. 链接ONNX Runtime
# set(ONNX_RUNTIME_DIR ${CMAKE_SOURCE_DIR}/../jniLibs)
# add_library(onnxruntime SHARED IMPORTED)
# set_target_properties(
#     onnxruntime
#     PROPERTIES IMPORTED_LOCATION
#     ${ONNX_RUNTIME_DIR}/${ANDROID_ABI}/libonnxruntime.so
# )

## 7. 链接所有库
# target_link_libraries(
#     funasr_android
#     android
#     log
#     yaml-cpp
#     kaldi-native-fbank
#     onnxruntime
# )

# ============================================
# 使用说明
# ============================================
# 第一阶段（当前）：
#   - 直接编译运行，测试JNI连接
#   - 返回模拟的识别结果
#
# 第二阶段（集成FunASR）：
#   1. 复制FunASR源码到 app/src/main/cpp/funasr/
#   2. 复制第三方库到 app/src/main/cpp/third_party/
#   3. 从ONNX Runtime AAR提取.so文件到 app/src/main/jniLibs/
#   4. 取消注释上面的代码
#   5. 重新编译
