# FunASR Android 集成详细指南

本文档提供详细的FunASR集成步骤。

## 📋 前置准备清单

- [ ] Android Studio已安装
- [ ] Android NDK已配置
- [ ] FunASR项目已克隆
- [ ] 模型文件已下载

---

## 第一步：复制FunASR C++源码

### 1.1 创建目录结构

```bash
cd /path/to/android-offline-demo/app/src/main/cpp/

# 创建目录
mkdir -p funasr/include
mkdir -p funasr/src
```

### 1.2 复制头文件

```bash
# 假设FunASR项目路径为 /home/user/FunASR
FUNASR_PATH=/home/user/FunASR

# 复制头文件
cp $FUNASR_PATH/runtime/onnxruntime/include/funasrruntime.h funasr/include/
cp $FUNASR_PATH/runtime/onnxruntime/include/com-define.h funasr/include/
cp $FUNASR_PATH/runtime/onnxruntime/include/audio.h funasr/include/
```

### 1.3 复制源文件

```bash
# 复制核心源文件
cp $FUNASR_PATH/runtime/onnxruntime/src/funasrruntime.cpp funasr/src/
cp $FUNASR_PATH/runtime/onnxruntime/src/paraformer.cpp funasr/src/
cp $FUNASR_PATH/runtime/onnxruntime/src/paraformer.h funasr/src/
cp $FUNASR_PATH/runtime/onnxruntime/src/paraformer-online.cpp funasr/src/
cp $FUNASR_PATH/runtime/onnxruntime/src/paraformer-online.h funasr/src/
cp $FUNASR_PATH/runtime/onnxruntime/src/audio.cpp funasr/src/
cp $FUNASR_PATH/runtime/onnxruntime/src/model.cpp funasr/src/
cp $FUNASR_PATH/runtime/onnxruntime/src/model.h funasr/src/
cp $FUNASR_PATH/runtime/onnxruntime/src/precomp.h funasr/src/

# 注意：可能还需要其他文件，根据编译错误添加
```

---

## 第二步：集成第三方库

### 2.1 yaml-cpp

```bash
# 复制整个yaml-cpp目录
cp -r $FUNASR_PATH/runtime/onnxruntime/third_party/yaml-cpp third_party/
```

### 2.2 kaldi-native-fbank

```bash
# 复制kaldi-native-fbank
cp -r $FUNASR_PATH/runtime/onnxruntime/third_party/kaldi-native-fbank third_party/
```

### 2.3 JSON库（header-only）

```bash
# 创建json目录
mkdir -p third_party/json/include

# 下载nlohmann/json单头文件
cd third_party/json/include
wget https://github.com/nlohmann/json/releases/download/v3.11.3/json.hpp
cd ../../..
```

### 2.4 OpenFST（可选，用于热词）

如果需要热词功能，复制OpenFST：

```bash
cp -r $FUNASR_PATH/runtime/onnxruntime/third_party/openfst third_party/
```

---

## 第三步：提取ONNX Runtime库

### 方式一：从Gradle缓存提取（推荐）

```bash
# 1. 先编译一次项目，让Gradle下载ONNX Runtime AAR
cd /path/to/android-offline-demo
./gradlew assembleDebug

# 2. 找到AAR文件
find ~/.gradle/caches -name "onnxruntime-android-*.aar"

# 假设找到的路径为：
# ~/.gradle/caches/modules-2/files-2.1/com.microsoft.onnxruntime/onnxruntime-android/1.16.3/xxxxx/onnxruntime-android-1.16.3.aar

# 3. 解压AAR（它是一个zip文件）
cd /tmp
unzip ~/.gradle/caches/modules-2/files-2.1/com.microsoft.onnxruntime/onnxruntime-android/1.16.3/xxxxx/onnxruntime-android-1.16.3.aar -d onnxruntime-extracted

# 4. 复制.so文件到项目
cd /path/to/android-offline-demo
mkdir -p app/src/main/jniLibs/arm64-v8a
mkdir -p app/src/main/jniLibs/armeabi-v7a

cp /tmp/onnxruntime-extracted/jni/arm64-v8a/libonnxruntime.so app/src/main/jniLibs/arm64-v8a/
cp /tmp/onnxruntime-extracted/jni/armeabi-v7a/libonnxruntime.so app/src/main/jniLibs/armeabi-v7a/

# 5. 复制头文件（用于CMake配置）
mkdir -p app/src/main/cpp/onnxruntime/include
cp -r /tmp/onnxruntime-extracted/headers/* app/src/main/cpp/onnxruntime/include/
```

### 方式二：手动下载

```bash
# 1. 访问 https://github.com/microsoft/onnxruntime/releases/tag/v1.16.3
# 2. 下载 onnxruntime-android-1.16.3.aar
# 3. 按照方式一的步骤3-5操作
```

---

## 第四步：下载模型文件

### 4.1 下载模型

```bash
# Paraformer在线量化模型（推荐）
git clone https://www.modelscope.cn/damo/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx.git

cd speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx

# 查看文件
ls -lh
# 应该看到：
# - model_quant.onnx (~120MB)
# - decoder_quant.onnx (~20MB)
# - config.yaml
# - am.mvn
# - tokens.json
```

### 4.2 复制模型到项目

```bash
cd /path/to/android-offline-demo

# 创建assets目录
mkdir -p app/src/main/assets/models/

# 复制模型文件
cp /path/to/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx/model_quant.onnx app/src/main/assets/models/
cp /path/to/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx/decoder_quant.onnx app/src/main/assets/models/
cp /path/to/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx/config.yaml app/src/main/assets/models/
cp /path/to/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx/am.mvn app/src/main/assets/models/
cp /path/to/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx/tokens.json app/src/main/assets/models/

# 验证
ls -lh app/src/main/assets/models/
```

---

## 第五步：修改CMakeLists.txt

打开 `app/src/main/cpp/CMakeLists.txt`，取消注释"第二阶段"部分：

```cmake
# 取消注释以下所有行：

# 1. 设置FunASR源码路径
set(FUNASR_SRC_DIR ${CMAKE_SOURCE_DIR}/funasr)
set(THIRD_PARTY_DIR ${CMAKE_SOURCE_DIR}/third_party)

# 2. 包含头文件
include_directories(
    ${FUNASR_SRC_DIR}/include
    ${THIRD_PARTY_DIR}
    ${THIRD_PARTY_DIR}/yaml-cpp/include
    ${THIRD_PARTY_DIR}/kaldi-native-fbank/kaldi-native-fbank/csrc
    ${THIRD_PARTY_DIR}/json/include
)

# ... 其余所有注释的代码
```

---

## 第六步：修改native-lib.cpp

打开 `app/src/main/cpp/native-lib.cpp`：

### 6.1 取消注释头文件

```cpp
// 第2-3行，取消注释：
#include "funasrruntime.h"
#include "com-define.h"
```

### 6.2 取消注释所有TODO标记的代码

搜索 `// TODO: 集成FunASR后取消注释`，取消所有相关代码的注释。

### 6.3 注释掉临时代码

注释掉所有 `// 临时返回值（集成FunASR前）` 的代码。

---

## 第七步：修改Kotlin代码

### 7.1 修改FunASREngine.kt

打开 `app/src/main/java/com/funasr/offline/FunASREngine.kt`：

在 `initialize()` 函数中，取消注释真实模型初始化代码：

```kotlin
fun initialize(): Boolean {
    try {
        Log.i(TAG, "Starting FunASR engine initialization...")

        val testResult = testConnection()
        Log.i(TAG, "JNI test: $testResult")

        // 取消注释以下代码块：
        // 1. 从assets复制模型到缓存目录
        val modelDir = copyModelsFromAssets()
        Log.i(TAG, "Models copied to: $modelDir")

        // 2. 初始化ASR模型
        asrHandle = native.nativeInit(modelDir, 2)
        if (asrHandle == 0L) {
            Log.e(TAG, "Failed to initialize ASR model")
            return false
        }

        // 3. 初始化在线句柄
        onlineHandle = native.nativeOnlineInit(asrHandle)
        if (onlineHandle == 0L) {
            Log.e(TAG, "Failed to initialize online handle")
            native.nativeUninit(asrHandle)
            return false
        }

        // 注释掉这两行临时代码：
        // asrHandle = 123456789L
        // onlineHandle = 987654321L

        isInitialized = true
        Log.i(TAG, "FunASR engine initialized successfully")
        return true

    } catch (e: Exception) {
        Log.e(TAG, "Initialization error", e)
        return false
    }
}
```

---

## 第八步：编译和测试

### 8.1 清理项目

```bash
cd /path/to/android-offline-demo
./gradlew clean
```

### 8.2 编译项目

```bash
./gradlew assembleDebug
```

**可能遇到的编译错误**：

#### 错误1: 找不到funasrruntime.h

**解决**: 检查文件是否存在于 `app/src/main/cpp/funasr/include/funasrruntime.h`

#### 错误2: undefined reference to `FunASRInit`

**解决**: 确保 `funasrruntime.cpp` 已添加到CMakeLists.txt的FUNASR_SOURCES中

#### 错误3: yaml-cpp编译错误

**解决**: 确保复制了完整的yaml-cpp目录

### 8.3 运行测试

1. 连接Android设备（推荐真机，模拟器可能性能不足）
2. 在Android Studio中点击 Run
3. 授予录音权限
4. 等待模型加载（首次运行会从assets复制模型，约20秒）
5. 长按录音按钮说话
6. 查看识别结果

### 8.4 查看日志

```bash
# 过滤FunASR相关日志
adb logcat | grep -E "FunASR|ONNX"

# 关键日志：
# - "Models copied to: ..." - 模型复制成功
# - "FunASR initialized successfully" - 初始化成功
# - "Recognition result: ..." - 识别结果
```

---

## 🐛 故障排查

### 问题1: 应用启动崩溃

**日志**: `UnsatisfiedLinkError: libonnxruntime.so`

**解决**:
```bash
# 检查.so文件是否存在
ls -l app/src/main/jniLibs/arm64-v8a/libonnxruntime.so
ls -l app/src/main/jniLibs/armeabi-v7a/libonnxruntime.so
```

### 问题2: 模型初始化失败

**日志**: `FunASR init failed`

**解决**:
1. 检查模型文件完整性
2. 确保所有5个文件都已复制
3. 检查assets目录权限

### 问题3: 识别无结果

**原因**:
- 音频格式错误
- 模型配置错误
- 音频过短

**解决**:
1. 确认AudioRecorder配置为16kHz, 16bit, Mono
2. 至少说1秒以上的话
3. 检查Logcat中的错误信息

---

## 📊 性能测试

### 测试指标

- **初始化时间**: 首次5-10秒（复制模型），后续1-2秒
- **内存占用**: 约300-400MB
- **RTF**: 实时率，理想值 < 0.3
- **延迟**: 端到端约200-500ms

### 优化建议

1. **预加载模型**: 应用启动时异步初始化
2. **调整线程数**: 根据设备CPU核心数
3. **使用NNAPI**: 启用Android硬件加速
4. **模型量化**: 已使用INT8量化模型

---

## ✅ 集成完成检查清单

- [ ] FunASR C++源码已复制
- [ ] 第三方库已复制
- [ ] ONNX Runtime .so文件已提取
- [ ] 模型文件已下载并复制
- [ ] CMakeLists.txt已修改
- [ ] native-lib.cpp已修改
- [ ] FunASREngine.kt已修改
- [ ] 项目编译成功
- [ ] 应用运行正常
- [ ] 模型初始化成功
- [ ] 能够进行语音识别

---

## 📞 获取帮助

如果遇到问题：

1. 查看Logcat日志
2. 检查本文档的故障排查部分
3. 在GitHub提交Issue
4. 参考FunASR官方文档

---

**祝集成顺利！🎉**
