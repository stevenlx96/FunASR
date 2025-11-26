# FunASR Android 离线识别 Demo

这是一个基于FunASR的Android离线语音识别Demo项目，支持在Android设备上进行本地ONNX模型推理。

## 📱 项目特点

- ✅ **完全离线** - 无需网络连接，保护隐私
- ✅ **本地推理** - 使用ONNX Runtime进行设备端推理
- ✅ **实时识别** - 支持流式语音识别
- ✅ **模块化设计** - JNI层、业务层分离，易于扩展
- ✅ **Material 3 UI** - 现代化的Compose UI

## 🏗️ 项目结构

```
FunASR-Offline/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── cpp/                     # JNI层（C++）
│   │   │   │   ├── CMakeLists.txt       # CMake配置
│   │   │   │   ├── native-lib.cpp       # JNI接口实现
│   │   │   │   ├── funasr/              # FunASR核心代码（待添加）
│   │   │   │   └── third_party/         # 第三方库（待添加）
│   │   │   ├── java/com/funasr/offline/
│   │   │   │   ├── FunASRNative.kt      # JNI接口声明
│   │   │   │   ├── FunASREngine.kt      # 引擎封装
│   │   │   │   ├── AudioRecorder.kt     # 音频录制
│   │   │   │   ├── MainActivity.kt      # 主界面
│   │   │   │   └── ui/theme/            # UI主题
│   │   │   ├── assets/models/           # 模型文件目录（待添加）
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── ...
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 🚀 快速开始

### 阶段一：测试项目框架（当前）

这个版本可以直接编译运行，用于测试JNI连接和Android项目框架。

#### 1. 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- Android SDK 34
- Android NDK 25.0+
- Gradle 8.2+
- JDK 17

#### 2. 导入项目

```bash
# 在Android Studio中：
File -> Open -> 选择 android-offline-demo 文件夹
```

#### 3. 同步Gradle

等待Gradle自动同步完成，下载所有依赖。

#### 4. 运行

- 连接Android设备或启动模拟器（Android 7.0+ / API 24+）
- 点击 Run 按钮
- 授予录音权限
- 长按录音按钮测试

**当前阶段输出**：返回模拟识别结果（用于测试框架）

---

### 阶段二：集成真实FunASR模型

完成第一阶段测试后，按以下步骤集成真实模型：

#### 1. 准备FunASR C++源码

```bash
# 进入项目cpp目录
cd app/src/main/cpp/

# 创建funasr目录
mkdir -p funasr/include funasr/src

# 从FunASR项目复制核心代码
# 假设FunASR项目在 /path/to/FunASR
cp /path/to/FunASR/runtime/onnxruntime/include/*.h funasr/include/
cp /path/to/FunASR/runtime/onnxruntime/src/*.cpp funasr/src/
cp /path/to/FunASR/runtime/onnxruntime/src/*.h funasr/src/
```

#### 2. 复制第三方依赖

```bash
# 复制第三方库
cp -r /path/to/FunASR/runtime/onnxruntime/third_party ./

# 主要包括：
# - yaml-cpp (YAML解析)
# - kaldi-native-fbank (特征提取)
# - nlohmann/json (JSON解析)
```

#### 3. 下载ONNX模型

```bash
# 下载Paraformer在线量化模型
git clone https://www.modelscope.cn/damo/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx.git

# 需要的文件：
# - model_quant.onnx (编码器, ~120MB)
# - decoder_quant.onnx (解码器, ~20MB)
# - config.yaml (配置文件)
# - am.mvn (CMVN参数)
# - tokens.json (词表)

# 复制到项目assets目录
mkdir -p app/src/main/assets/models/
cp speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx/{model_quant.onnx,decoder_quant.onnx,config.yaml,am.mvn,tokens.json} app/src/main/assets/models/
```

#### 4. 提取ONNX Runtime .so文件

ONNX Runtime AAR包含在项目依赖中，但需要提取.so文件：

```bash
# 方式1：从AAR提取
# 1. 编译一次项目，Gradle会下载AAR到缓存
# 2. 找到AAR文件（通常在 ~/.gradle/caches/modules-2/files-2.1/com.microsoft.onnxruntime/...）
# 3. 解压AAR（它是一个zip文件）
# 4. 复制 jni/{arm64-v8a,armeabi-v7a}/libonnxruntime.so 到项目

mkdir -p app/src/main/jniLibs/arm64-v8a
mkdir -p app/src/main/jniLibs/armeabi-v7a

# 复制.so文件
cp /path/to/extracted/jni/arm64-v8a/libonnxruntime.so app/src/main/jniLibs/arm64-v8a/
cp /path/to/extracted/jni/armeabi-v7a/libonnxruntime.so app/src/main/jniLibs/armeabi-v7a/

# 方式2：从ONNX Runtime官方下载
# 访问 https://github.com/microsoft/onnxruntime/releases
# 下载 onnxruntime-android-1.16.3.aar
```

#### 5. 修改CMakeLists.txt

打开 `app/src/main/cpp/CMakeLists.txt`，取消注释"第二阶段"部分的代码。

#### 6. 修改native-lib.cpp

打开 `app/src/main/cpp/native-lib.cpp`，取消注释以下内容：
- 头文件引用
- FunASR API调用

#### 7. 修改FunASREngine.kt

打开 `app/src/main/java/com/funasr/offline/FunASREngine.kt`，取消注释模型初始化代码。

#### 8. 重新编译运行

```bash
# 清理项目
./gradlew clean

# 编译
./gradlew assembleDebug

# 或在Android Studio中点击 Run
```

---

## 📖 核心API说明

### FunASREngine

```kotlin
val engine = FunASREngine(context)

// 初始化
val success = engine.initialize()

// 推理
val result = engine.infer(audioData, isFinished = false)

// 重置
engine.reset()

// 释放
engine.release()
```

### AudioRecorder

```kotlin
val recorder = AudioRecorder { audioData ->
    // 处理音频数据
    val result = engine.infer(audioData)
}

recorder.startRecording()
recorder.stopRecording()
```

---

## 🎯 音频参数

模型要求的音频格式：

- **采样率**: 16kHz
- **位深**: 16bit
- **声道**: 单声道 (Mono)
- **格式**: PCM
- **缓冲大小**: 960采样点 (60ms)

---

## 🔧 常见问题

### Q1: Gradle同步失败

**错误**: `Unresolved reference: android`

**解决**: 你的build.gradle文件使用了Groovy语法，但项目是Kotlin DSL。使用本项目提供的 `build.gradle.kts` 文件。

### Q2: CMake编译失败

**错误**: `Cannot find funasrruntime.h`

**解决**: 确保已复制FunASR C++源码到 `app/src/main/cpp/funasr/` 目录。

### Q3: 运行时崩溃 - UnsatisfiedLinkError

**错误**: `java.lang.UnsatisfiedLinkError: dlopen failed: library "libonnxruntime.so" not found`

**解决**: 确保已将 `libonnxruntime.so` 放入 `app/src/main/jniLibs/{ABI}/` 目录。

### Q4: 模型加载失败

**错误**: `FunASR init failed`

**解决**:
1. 检查模型文件是否完整（5个文件）
2. 检查 assets/models/ 目录结构
3. 查看Logcat日志获取详细错误信息

### Q5: 识别结果为空

**可能原因**:
1. 音频格式不匹配（必须16kHz, 16bit, Mono）
2. 音频数据太短
3. 模型配置错误

---

## 📊 性能优化建议

### 1. 模型量化

使用INT8量化模型（`model_quant.onnx`）而不是FP32模型，可以：
- 减小模型体积 70%
- 提升推理速度 2-3倍
- 降低内存占用

### 2. 线程配置

```kotlin
// 根据设备性能调整线程数
val threadNum = if (cores >= 8) 4 else 2
engine.nativeInit(modelDir, threadNum)
```

### 3. NNAPI加速

ONNX Runtime支持Android NNAPI硬件加速，可以在初始化时配置。

### 4. 内存管理

- 及时调用 `engine.release()`
- 避免多次初始化模型
- 使用流式处理，避免加载大文件

---

## 📝 更新日志

### v1.0.0 (2025-01-26)

- ✅ 创建Android项目框架
- ✅ 实现JNI接口层
- ✅ 实现音频录制模块
- ✅ 实现Compose UI界面
- ✅ 提供完整的集成文档
- ⏳ 待集成：FunASR C++代码
- ⏳ 待集成：ONNX模型文件

---

## 🤝 参考资源

- [FunASR GitHub](https://github.com/alibaba-damo-academy/FunASR)
- [ONNX Runtime Android](https://onnxruntime.ai/docs/build/android.html)
- [Paraformer模型](https://www.modelscope.cn/models/damo/speech_paraformer-large_asr_nat-zh-cn-16k-common-vocab8404-online-onnx)
- [Android NDK文档](https://developer.android.com/ndk)

---

## 📄 许可证

本项目基于MIT许可证开源。

FunASR核心库遵循其原始许可证。

---

## 👥 贡献

欢迎提交Issue和Pull Request！

---

## 📮 联系方式

如有问题，请在GitHub Issues中提出。

---

**Enjoy coding! 🎉**
