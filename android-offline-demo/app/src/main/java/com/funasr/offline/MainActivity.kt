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
                val success = asrEngine.initialize()
                isInitialized = success
                statusText = if (success) {
                    "✓ 引擎已就绪，长按按钮开始录音"
                } else {
                    "✗ 引擎初始化失败"
                }
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

                // 录音按钮（长按录音）
                Button(
                    onClick = { /* 点击无效，需要长按 */ },
                    modifier = Modifier
                        .size(120.dp)
                        .pointerInput(isInitialized) {
                            detectTapGestures(
                                onPress = {
                                    if (isInitialized) {
                                        // 按下 - 开始录音
                                        isRecording = true
                                        startRecognition(
                                            onResult = { result ->
                                                recognitionText = result
                                            }
                                        )
                                        tryAwaitRelease()
                                        // 释放 - 停止录音
                                        isRecording = false
                                        stopRecognition(
                                            onFinalResult = { result ->
                                                recognitionText = result
                                            }
                                        )
                                    }
                                }
                            )
                        },
                    enabled = isInitialized,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isRecording) "录音中" else "长按\n录音",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 说明文字
                Text(
                    text = if (isInitialized) "按住按钮说话，松开结束" else "等待初始化...",
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
        // 重置状态
        asrEngine.reset()

        // 开始录音
        audioRecorder = AudioRecorder { audioData ->
            // 推理音频数据
            val result = asrEngine.infer(audioData, isFinished = false)
            if (result.isNotEmpty()) {
                runOnUiThread {
                    onResult(result)
                }
            }
        }
        audioRecorder?.startRecording()
    }

    /**
     * 停止识别
     */
    private fun stopRecognition(onFinalResult: (String) -> Unit) {
        // 停止录音
        audioRecorder?.stopRecording()
        audioRecorder = null

        // 发送结束信号，获取最终结果
        val finalResult = asrEngine.infer(ByteArray(0), isFinished = true)
        runOnUiThread {
            onFinalResult(finalResult)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecorder?.stopRecording()
        asrEngine.release()
    }
}
