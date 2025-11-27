package com.funasr.offline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.funasr.offline.ui.theme.FunASROfflineTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var asrEngine: FunASREngine
    private var audioRecorder: AudioRecorder? = null

    // 权限请求
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已授予
        } else {
            // 权限被拒绝
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 请求录音权限
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // 初始化ASR引擎
        asrEngine = FunASREngine(this)

        setContent {
            FunASROfflineTheme {
                FunASRScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FunASRScreen() {
        var recognitionText by remember { mutableStateOf("") }
        var statusText by remember { mutableStateOf("正在初始化引擎...") }
        var isRecording by remember { mutableStateOf(false) }
        var isInitialized by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        // 初始化引擎
        LaunchedEffect(Unit) {
            scope.launch {
                android.util.Log.i("MainActivity", "LaunchedEffect: Starting engine initialization...")
                val success = asrEngine.initialize()
                android.util.Log.i("MainActivity", "Engine initialization result: $success")
                isInitialized = success
                android.util.Log.i("MainActivity", "isInitialized state set to: $isInitialized")
                statusText = if (success) {
                    "✓ 引擎已就绪，点击按钮开始录音"
                } else {
                    "✗ 引擎初始化失败"
                }
                android.util.Log.i("MainActivity", "Status text: $statusText")
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("FunASR 离线识别 Demo") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 顶部状态信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isInitialized)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 识别结果显示区域
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "识别结果：",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (recognitionText.isEmpty()) "暂无识别结果" else recognitionText,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 录音按钮状态指示
                if (isRecording) {
                    Text(
                        text = "🎤 正在录音...",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 录音按钮（点击开始/停止录音）
                Button(
                    onClick = {
                        android.util.Log.i("MainActivity", "========== BUTTON CLICKED ==========")
                        android.util.Log.d("MainActivity", "isRecording=$isRecording, isInitialized=$isInitialized")

                        if (isRecording) {
                            // 当前正在录音 - 停止录音
                            android.util.Log.i("MainActivity", "Stopping recording...")
                            isRecording = false
                            stopRecognition(
                                onFinalResult = { result ->
                                    recognitionText = result
                                }
                            )
                        } else {
                            // 当前未录音 - 开始录音
                            android.util.Log.i("MainActivity", "Starting recording...")
                            isRecording = true
                            startRecognition(
                                onResult = { result ->
                                    recognitionText = result
                                }
                            )
                        }
                    },
                    modifier = Modifier.size(120.dp),
                    enabled = isInitialized,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isRecording) "停止" else "开始\n录音",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 说明文字
                Text(
                    text = when {
                        !isInitialized -> "等待初始化..."
                        isRecording -> "点击停止按钮结束录音"
                        else -> "点击按钮开始录音"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    /**
     * 开始识别
     */
    private fun startRecognition(onResult: (String) -> Unit) {
        android.util.Log.i("MainActivity", "========== START RECOGNITION ==========")
        try {
            // 重置状态
            android.util.Log.d("MainActivity", "Resetting ASR engine...")
            asrEngine.reset()

            // 开始录音
            android.util.Log.d("MainActivity", "Creating AudioRecorder...")
            audioRecorder = AudioRecorder { audioData ->
                android.util.Log.v("MainActivity", "Received audio data: ${audioData.size} bytes")
                // 推理音频数据
                val result = asrEngine.infer(audioData, isFinished = false)
                android.util.Log.d("MainActivity", "Inference result: $result")
                if (result.isNotEmpty()) {
                    runOnUiThread {
                        onResult(result)
                    }
                }
            }
            android.util.Log.d("MainActivity", "Starting recording...")
            audioRecorder?.startRecording()
            android.util.Log.i("MainActivity", "Recording started successfully!")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in startRecognition", e)
        }
    }

    /**
     * 停止识别
     */
    private fun stopRecognition(onFinalResult: (String) -> Unit) {
        android.util.Log.i("MainActivity", "========== STOP RECOGNITION ==========")
        try {
            // 停止录音
            android.util.Log.d("MainActivity", "Stopping recording...")
            audioRecorder?.stopRecording()
            audioRecorder = null

            // 发送结束信号，获取最终结果
            android.util.Log.d("MainActivity", "Getting final result...")
            val finalResult = asrEngine.infer(ByteArray(0), isFinished = true)
            android.util.Log.i("MainActivity", "Final result: $finalResult")
            runOnUiThread {
                onFinalResult(finalResult)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in stopRecognition", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder?.stopRecording()
        asrEngine.release()
    }
}
