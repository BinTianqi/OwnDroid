package com.bintianqi.owndroid

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ManageSpaceActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val sharedPref = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        val materialYou = sharedPref.getBoolean("material_you",true)
        val blackTheme = sharedPref.getBoolean("black_theme", false)
        setContent {
            OwnDroidTheme(materialYou, blackTheme) {
                ManageSpace()
            }
        }
    }

    @Composable
    fun ManageSpace() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            val sharedPref = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
            val protected = sharedPref.getBoolean("protect_storage", false)
            if(protected){
                Text(
                    text = stringResource(R.string.storage_is_protected),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.you_cant_clear_storage),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Button(
                onClick = {
                    lifecycleScope.launch {
                        sharedPref.edit().clear().apply()
                        delay(2000)
                        finishAndRemoveTask()
                    }
                    Toast.makeText(applicationContext, R.string.clear_storage_success, Toast.LENGTH_SHORT).show()
                },
                enabled = !protected,
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.onError,
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(text = stringResource(R.string.clear_storage))
            }
        }
    }
}