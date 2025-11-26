package com.funasr.offline

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * FunASR 引擎封装
 * 负责模型初始化、推理和资源管理
 */
class FunASREngine(private val context: Context) {

    private val TAG = "FunASREngine"
    private val native = FunASRNative()

    private var asrHandle: Long = 0
    private var onlineHandle: Long = 0
    private var isInitialized = false

    /**
     * 测试JNI连接
     */
    fun testConnection(): String {
        return try {
            native.nativeTestConnection()
        } catch (e: Exception) {
            Log.e(TAG, "JNI connection test failed", e)
            "JNI connection failed: ${e.message}"
        }
    }

    /**
     * 初始化引擎
     * @return 是否初始化成功
     */
    fun initialize(): Boolean {
        try {
            Log.i(TAG, "Starting FunASR engine initialization...")

            // 测试JNI连接
            val testResult = testConnection()
            Log.i(TAG, "JNI test: $testResult")

            // TODO: 集成真实模型后取消注释
            /*
            // 1. 从assets复制模型到缓存目录
            val modelDir = copyModelsFromAssets()
            Log.i(TAG, "Models copied to: $modelDir")

            // 2. 初始化ASR模型
            asrHandle = native.nativeInit(modelDir, 2)  // 使用2个线程
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
            */

            // 临时模拟初始化（测试阶段）
            asrHandle = 123456789L
            onlineHandle = 987654321L

            isInitialized = true
            Log.i(TAG, "FunASR engine initialized successfully")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Initialization error", e)
            return false
        }
    }

    /**
     * 推理音频数据
     * @param audioData PCM音频数据（16位，16kHz，单声道）
     * @param isFinished 是否是最后一帧
     * @return 识别结果（JSON字符串）
     */
    fun infer(audioData: ByteArray, isFinished: Boolean = false): String {
        if (!isInitialized) {
            Log.e(TAG, "Engine not initialized")
            return "{\"error\":\"Engine not initialized\"}"
        }

        return try {
            native.nativeInfer(onlineHandle, audioData, isFinished)
        } catch (e: Exception) {
            Log.e(TAG, "Inference error", e)
            "{\"error\":\"${e.message}\"}"
        }
    }

    /**
     * 重置状态（开始新的识别）
     */
    fun reset() {
        if (isInitialized) {
            try {
                native.nativeReset(onlineHandle)
                Log.i(TAG, "Engine reset")
            } catch (e: Exception) {
                Log.e(TAG, "Reset error", e)
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        if (isInitialized) {
            try {
                native.nativeUninit(asrHandle)
                isInitialized = false
                Log.i(TAG, "FunASR engine released")
            } catch (e: Exception) {
                Log.e(TAG, "Release error", e)
            }
        }
    }

    /**
     * 从assets复制模型文件到缓存目录
     * TODO: 集成真实模型后使用此函数
     */
    private fun copyModelsFromAssets(): String {
        val modelDir = File(context.cacheDir, "models")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }

        val modelFiles = listOf(
            "model_quant.onnx",
            "decoder_quant.onnx",
            "config.yaml",
            "am.mvn",
            "tokens.json"
        )

        for (fileName in modelFiles) {
            val destFile = File(modelDir, fileName)
            if (!destFile.exists()) {
                try {
                    Log.i(TAG, "Copying $fileName...")
                    context.assets.open("models/$fileName").use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.i(TAG, "$fileName copied successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to copy $fileName", e)
                    throw e
                }
            } else {
                Log.i(TAG, "$fileName already exists, skipping")
            }
        }

        return modelDir.absolutePath
    }
}
