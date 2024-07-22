package com.posite.ketchex

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ketch.DownloadConfig
import com.ketch.Ketch
import com.ketch.NotificationConfig
import com.posite.ketchex.ui.theme.KetchExTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var ketch: Ketch
    private var permmision = mutableStateOf(false)
    private var oneAct = true

    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val deniedPermissionList = permissions.filter { !it.value }.map { it.key }
            when {
                deniedPermissionList.isNotEmpty() -> {
                    val map = deniedPermissionList.groupBy { permission ->
                        if (shouldShowRequestPermissionRationale(permission)) DENIED else EXPLAINED
                    }
                    map[DENIED]?.let {
                        // 단순히 권한이 거부 되었을 때
                    }
                    map[EXPLAINED]?.let {
                        // 권한 요청이 완전히 막혔을 때(주로 앱 상세 창 열기)
                    }
                }

                else -> {
                    ketch = Ketch.init(
                        context = this,
                        notificationConfig = NotificationConfig(
                            enabled = true, //Default: false
                            smallIcon = R.drawable.ic_download, //It is required
                        ),
                        downloadConfig = DownloadConfig(
                            connectTimeOutInMs = 20000L, //Default: 10000L
                            readTimeOutInMs = 15000L //Default: 10000L
                        )
                    )
                    permmision.value = true
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                registerForActivityResult.launch(
                    arrayOf(
                        android.Manifest.permission.POST_NOTIFICATIONS,
                        android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
                    )
                )
            } else {
                registerForActivityResult.launch(
                    arrayOf(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            }
        }
        setContent {
            KetchExTheme {
                if (permmision.value && oneAct) {
                    Log.d("Ketch_", "K")
                    oneAct = false
                    DownloadProfileImageScreen(ketch, viewModel)
                }
            }
        }
    }

    companion object {
        const val DENIED = "denied"
        const val EXPLAINED = "explained"
    }
}

@Composable
fun DownloadProfileImageScreen(ketch: Ketch, viewModel: MainViewModel) {
    Log.d("Ketch_", "download start")

    val context = LocalContext.current
    DownloadState(viewModel, context)
    ketch.download(
        stringResource(id = R.string.profile_url),
        fileName = "Ketch-master.zip",
        onStart = {
            Log.d("Ketch_start", "size: $it")
            viewModel.setSize(it)
        },
        onProgress = { progress, downloadSpeed ->
            Log.d("Ketch_progress", "$progress, $downloadSpeed")
            viewModel.setProgress(progress / 100f)
            viewModel.setSpeed(downloadSpeed)
        },
        onFailure = {
            Log.d("Ketch_fail", it)
        },
        onSuccess = {
            Log.d("Ketch_success", "Success")
        }

    )
}

@Composable
private fun DownloadState(
    viewModel: MainViewModel,
    context: Context
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        DownloadProgress(progress = viewModel.progress.value)
        Spacer(modifier = Modifier.height(8.dp))
        DownloadSpeed(downloadSpeed = viewModel.speed.value, context = context)
    }
}

@Composable
fun DownloadProgress(progress: Float) {
    LinearProgressIndicator(
        progress = { progress },
        color = colorResource(id = R.color.highlight),
        trackColor = colorResource(id = R.color.teal_200),
        strokeCap = StrokeCap.Round
    )
}

@Composable
fun DownloadSpeed(downloadSpeed: Float, context: Context) {
    Text(text = "Download Speed: $downloadSpeed B/s")
    //Toast.makeText(context, "Download Speed: $downloadSpeed B/s", Toast.LENGTH_SHORT).show()
}


@Preview(showBackground = true)
@Composable
fun DownloadProgressPreview() {
    KetchExTheme {
        DownloadProgress(0.5f)
    }
}