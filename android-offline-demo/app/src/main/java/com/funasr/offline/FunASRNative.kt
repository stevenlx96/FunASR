package com.funasr.offline

/**
 * FunASR Native JNI 接口
 * 负责与C++层交互
 */
class FunASRNative {

    companion object {
        init {
            System.loadLibrary("funasr_android")
        }
    }

    /**
     * 测试JNI连接
     * @return 测试消息
     */
    external fun nativeTestConnection(): String

    /**
     * 初始化ASR模型
     * @param modelDir 模型目录路径
     * @param threadNum 线程数（建议1-4）
     * @return ASR模型句柄
     */
    external fun nativeInit(modelDir: String, threadNum: Int): Long

    /**
     * 初始化在线流式句柄
     * @param asrHandle ASR模型句柄
     * @return 在线句柄
     */
    external fun nativeOnlineInit(asrHandle: Long): Long

    /**
     * 推理音频数据
     * @param onlineHandle 在线句柄
     * @param audioData 音频PCM数据（16位）
     * @param isFinished 是否是最后一帧
     * @return 识别结果（JSON字符串）
     */
    external fun nativeInfer(
        onlineHandle: Long,
        audioData: ByteArray,
        isFinished: Boolean
    ): String

    /**
     * 重置在线状态（开始新的识别）
     * @param onlineHandle 在线句柄
     */
    external fun nativeReset(onlineHandle: Long)

    /**
     * 释放资源
     * @param asrHandle ASR模型句柄
     */
    external fun nativeUninit(asrHandle: Long)
}
