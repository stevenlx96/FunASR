package com.funasr.offline

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*

/**
 * 音频录制器
 * 录制16kHz、16bit、单声道PCM音频
 */
class AudioRecorder(
    private val onAudioData: (ByteArray) -> Unit
) {
    private val TAG = "AudioRecorder"

    // 音频参数（必须与模型匹配）
    private val sampleRate = 16000  // 16kHz
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    // 缓冲区大小：960字节（60ms）
    // 960采样点 * 2字节 = 1920字节
    private val bufferSizeInSamples = 960
    private val bufferSizeInBytes = bufferSizeInSamples * 2

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var isRecording = false

    /**
     * 开始录音
     */
    fun startRecording() {
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return
        }

        try {
            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioFormat
            )

            if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(TAG, "Invalid buffer size: $minBufferSize")
                return
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                maxOf(minBufferSize, bufferSizeInBytes * 4)
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            isRecording = true
            Log.i(TAG, "Recording started (sampleRate=$sampleRate, bufferSize=$bufferSizeInBytes)")

            // 在后台线程读取音频数据
            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val buffer = ByteArray(bufferSizeInBytes)
                var totalBytesRead = 0

                while (isActive && isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                    if (readSize > 0) {
                        totalBytesRead += readSize
                        // 将音频数据传递给回调
                        onAudioData(buffer.copyOf(readSize))

                        // 每秒打印一次统计信息
                        if (totalBytesRead % (sampleRate * 2) < bufferSizeInBytes) {
                            Log.d(TAG, "Read ${totalBytesRead / 1024}KB audio data")
                        }
                    } else if (readSize < 0) {
                        Log.e(TAG, "Read error: $readSize")
                        break
                    }
                }

                Log.i(TAG, "Recording loop ended, total read: ${totalBytesRead / 1024}KB")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            stopRecording()
        }
    }

    /**
     * 停止录音
     */
    fun stopRecording() {
        isRecording = false

        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.i(TAG, "Recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
        }
    }

    /**
     * 检查是否正在录音
     */
    fun isRecording(): Boolean = isRecording
}
