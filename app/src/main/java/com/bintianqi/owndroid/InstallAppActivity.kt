package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import com.bintianqi.owndroid.dpm.installPackage
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme

class InstallAppActivity: FragmentActivity() {
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sharedPref = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContent {
            var installing by remember { mutableStateOf(false) }
            OwnDroidTheme(
                sharedPref.getBoolean("material_you", true),
                sharedPref.getBoolean("black_theme", false)
            ) {
                AlertDialog(
                    title = {
                        Text(stringResource(R.string.install_app))
                    },
                    onDismissRequest = {
                        finish()
                    },
                    text = {
                        AnimatedVisibility(installing) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { finish() }) { Text(stringResource(R.string.cancel)) }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                installing = true
                                uriToStream(applicationContext, this.intent.data) { stream -> installPackage(applicationContext, stream) }
                            }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            val installDone by installAppDone.collectAsState()
            LaunchedEffect(installDone) {
                if(installDone) finish()
            }
        }
    }
}
