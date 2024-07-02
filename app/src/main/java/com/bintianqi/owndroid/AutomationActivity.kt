package com.bintianqi.owndroid

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text

class AutomationActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val result = handleTask(applicationContext, this.intent)
        val sharedPrefs = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        if(sharedPrefs.getBoolean("automation_debug", false)) {
            setContent {
                SelectionContainer {
                    Text(result)
                }
            }
        } else {
            finish()
        }
    }
}