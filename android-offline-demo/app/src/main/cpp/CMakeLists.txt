cmake_minimum_required(VERSION 3.22.1)
project("funasr_android")

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -Wall -Wno-sign-compare")

# FunASR 源码路径
set(FUNASR_SRC_DIR "${CMAKE_CURRENT_SOURCE_DIR}/funasr")
set(THIRD_PARTY_DIR "${CMAKE_CURRENT_SOURCE_DIR}/third_party")

# 包含头文件
include_directories(
    "${FUNASR_SRC_DIR}/include"
    "${FUNASR_SRC_DIR}/src"
    "${THIRD_PARTY_DIR}"
    "${THIRD_PARTY_DIR}/yaml-cpp/include"
    "${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc"
)

# 添加 FunASR 核心源文件（显式列出，避免 GLOB）
set(FUNASR_SOURCES
    "${FUNASR_SRC_DIR}/src/funasrruntime.cpp"
    "${FUNASR_SRC_DIR}/src/paraformer.cpp"
    "${FUNASR_SRC_DIR}/src/model.cpp"
    "${FUNASR_SRC_DIR}/src/audio.cpp"
    "${FUNASR_SRC_DIR}/src/vocab.cpp"
    "${FUNASR_SRC_DIR}/src/util.cpp"
    "${FUNASR_SRC_DIR}/src/tokenizer.cpp"
    "${FUNASR_SRC_DIR}/src/phone-set.cpp"
)

# yaml-cpp 源文件
set(YAML_CPP_SOURCES
    "${THIRD_PARTY_DIR}/yaml-cpp/src/binary.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/convert.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/directives.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/emit.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/emitfromevents.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/emitter.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/emitterstate.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/emitterutils.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/exceptions.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/exp.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/memory.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/node.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/node_data.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/nodebuilder.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/nodeevents.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/null.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/ostream_wrapper.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/parse.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/parser.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/regex_yaml.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/scanner.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/scanscalar.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/scantag.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/scantoken.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/simplekey.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/singledocparser.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/stream.cpp"
    "${THIRD_PARTY_DIR}/yaml-cpp/src/tag.cpp"
)

# kaldi-native-fbank 源文件
set(KALDI_FBANK_SOURCES
    "${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc/feature-fbank.cc"
    "${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc/feature-functions.cc"
    "${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc/feature-window.cc"
    "${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc/fftsg.c"
    "${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc/mel-computations.cc"
    "${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc/online-feature.cc"
    "${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc/rfft.cc"
)

# 创建共享库
add_library(
    funasr_android
    SHARED
    native-lib.cpp
    ${FUNASR_SOURCES}
    ${YAML_CPP_SOURCES}
    ${KALDI_FBANK_SOURCES}
)

# 链接 ONNX Runtime
set(ONNX_RUNTIME_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs")
add_library(onnxruntime SHARED IMPORTED)
set_target_properties(
    onnxruntime
    PROPERTIES IMPORTED_LOCATION
    "${ONNX_RUNTIME_DIR}/${ANDROID_ABI}/libonnxruntime.so"
)

# 链接所有库
target_link_libraries(
    funasr_android
    android
    log
    onnxruntime
)
