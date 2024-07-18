package com.bintianqi.owndroid

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext

class AutomationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val result = handleTask(applicationContext, this.intent)
        val sharedPrefs = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        if(sharedPrefs.getBoolean("automation_debug", false)) {
            setContent {
                AlertDialog.Builder(LocalContext.current)
                    .setMessage(result)
                    .setOnDismissListener { finish() }
                    .setPositiveButton(R.string.confirm) { _, _ -> finish() }
                    .show()
            }
        } else {
            finish()
        }
    }
}