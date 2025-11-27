#include <jni.h>
#include <string>
#include <android/log.h>
#include <map>

// FunASR headers
#include "funasrruntime.h"
#include "com-define.h"

#define LOG_TAG "FunASR-JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

/**
 * 示例JNI函数 - 测试JNI是否正常工作
 */
JNIEXPORT jstring JNICALL
Java_com_funasr_offline_FunASRNative_nativeTestConnection(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "FunASR JNI Connected Successfully!";
    LOGI("Test connection called");
    return env->NewStringUTF(hello.c_str());
}

/**
 * 初始化ASR模型
 * @param modelDir 模型目录路径
 * @param threadNum 线程数
 * @return 模型句柄（long类型）
 */
JNIEXPORT jlong JNICALL
Java_com_funasr_offline_FunASRNative_nativeInit(
        JNIEnv *env,
        jobject thiz,
        jstring modelDir,
        jint threadNum) {

    const char *model_path = env->GetStringUTFChars(modelDir, nullptr);
    LOGI("Initializing FunASR with model dir: %s, threads: %d", model_path, threadNum);

    std::map<std::string, std::string> model_paths;
    model_paths.insert({MODEL_DIR, model_path});
    model_paths.insert({QUANTIZE, "true"});  // 使用量化模型

    // 初始化ASR模型（在线模式）
    FUNASR_HANDLE asr_handle = FunASRInit(model_paths, threadNum, ASR_ONLINE);

    env->ReleaseStringUTFChars(modelDir, model_path);

    if (!asr_handle) {
        LOGE("Failed to initialize FunASR");
        return 0;
    }

    LOGI("FunASR initialized successfully, handle: %p", asr_handle);
    return (jlong) asr_handle;
}

/**
 * 初始化在线流式句柄
 * @param asrHandle ASR模型句柄
 * @return 在线句柄（long类型）
 */
JNIEXPORT jlong JNICALL
Java_com_funasr_offline_FunASRNative_nativeOnlineInit(
        JNIEnv *env,
        jobject thiz,
        jlong asrHandle) {

    LOGI("Initializing online handle from ASR handle: %lld", (long long)asrHandle);

    FUNASR_HANDLE asr_handle = (FUNASR_HANDLE) asrHandle;

    // 初始化在线流式特征
    // chunk_size: {5,10,5} 表示左侧5帧，中间10帧，右侧5帧
    std::vector<int> chunk_size = {5, 10, 5};
    FUNASR_HANDLE online_handle = FunASROnlineInit(asr_handle, chunk_size);

    if (!online_handle) {
        LOGE("Failed to initialize online handle");
        return 0;
    }

    LOGI("Online handle initialized successfully, handle: %p", online_handle);
    return (jlong) online_handle;
}

/**
 * 推理音频数据
 * @param onlineHandle 在线句柄
 * @param audioData 音频PCM数据（16位）
 * @param isFinished 是否是最后一帧
 * @return 识别结果JSON字符串
 */
JNIEXPORT jstring JNICALL
Java_com_funasr_offline_FunASRNative_nativeInfer(
        JNIEnv *env,
        jobject thiz,
        jlong onlineHandle,
        jbyteArray audioData,
        jboolean isFinished) {

    // 获取音频数据
    jbyte *audio_buffer = env->GetByteArrayElements(audioData, nullptr);
    jsize audio_len = env->GetArrayLength(audioData);

    LOGD("Inferring audio data, handle: %lld, length: %d, finished: %d",
         (long long)onlineHandle, audio_len, isFinished);

    FUNASR_HANDLE online_handle = (FUNASR_HANDLE) onlineHandle;

    // 调用推理
    FUNASR_RESULT result = FunASRInferBuffer(
            online_handle,
            (const char *) audio_buffer,
            audio_len,
            RASR_NONE,
            nullptr,
            isFinished,
            16000,  // 采样率16kHz
            "pcm"
    );

    env->ReleaseByteArrayElements(audioData, audio_buffer, JNI_ABORT);

    if (!result) {
        LOGE("Inference failed");
        return env->NewStringUTF("");
    }

    // 获取识别结果
    const char *result_text = FunASRGetResult(result, 0);
    jstring jresult = env->NewStringUTF(result_text ? result_text : "");

    LOGI("Recognition result: %s", result_text ? result_text : "(empty)");

    // 释放结果
    FunASRFreeResult(result);

    return jresult;
}

/**
 * 重置在线状态
 * @param onlineHandle 在线句柄
 */
JNIEXPORT void JNICALL
Java_com_funasr_offline_FunASRNative_nativeReset(
        JNIEnv *env,
        jobject thiz,
        jlong onlineHandle) {

    LOGI("Resetting online handle: %lld", (long long)onlineHandle);

    FUNASR_HANDLE online_handle = (FUNASR_HANDLE) onlineHandle;
    FunASRReset(online_handle);

    LOGI("Online handle reset successfully");
}

/**
 * 释放资源
 * @param asrHandle ASR模型句柄
 */
JNIEXPORT void JNICALL
Java_com_funasr_offline_FunASRNative_nativeUninit(
        JNIEnv *env,
        jobject thiz,
        jlong asrHandle) {

    LOGI("Uninitializing FunASR handle: %lld", (long long)asrHandle);

    FUNASR_HANDLE asr_handle = (FUNASR_HANDLE) asrHandle;
    FunASRUninit(asr_handle);

    LOGI("FunASR uninitialized successfully");
}

} // extern "C"
