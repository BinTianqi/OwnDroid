package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bintianqi.owndroid.dpm.installPackage
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import com.github.fishb1.apkinfo.ApkInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream

class InstallAppActivity: FragmentActivity() {
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val context = applicationContext
        val sharedPref = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val uri = this.intent.data!!
        var apkInfoText by mutableStateOf(context.getString(R.string.parsing_apk_info))
        var status by mutableStateOf("parsing")
        this.lifecycleScope.launch(Dispatchers.IO) {
            val fd = applicationContext.contentResolver.openFileDescriptor(uri, "r")
            val apkInfo = ApkInfo.fromInputStream(
                FileInputStream(fd?.fileDescriptor)
            )
            fd?.close()
            withContext(Dispatchers.Main) {
                status = "waiting"
                apkInfoText = "${context.getString(R.string.package_name)}: ${apkInfo.packageName}\n"
                apkInfoText += "${context.getString(R.string.version_name)}: ${apkInfo.versionName}\n"
                apkInfoText += "${context.getString(R.string.version_code)}: ${apkInfo.versionCode}"
            }
        }
        setContent {
            OwnDroidTheme(
                sharedPref.getBoolean("material_you", true),
                sharedPref.getBoolean("black_theme", false)
            ) {
                AlertDialog(
                    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                    title = {
                        Text(stringResource(R.string.install_app))
                    },
                    onDismissRequest = {
                        if(status != "installing") finish()
                    },
                    text = {
                        Column {
                            AnimatedVisibility(status != "waiting") {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                            Text(text = apkInfoText, modifier = Modifier.padding(top = 4.dp))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { finish() },
                            enabled = status != "installing"
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                status = "installing"
                                uriToStream(applicationContext, this.intent.data) { stream -> installPackage(applicationContext, stream) }
                            },
                            enabled = status != "installing"
                        ) {
                            Text(stringResource(R.string.install))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            val installDone by installAppDone.collectAsState()
            LaunchedEffect(installDone) {
                if(installDone) {
                    installAppDone.value = false
                    finish()
                }
            }
        }
    }
}
