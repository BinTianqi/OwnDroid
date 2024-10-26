package com.bintianqi.owndroid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme

class ManageSpaceActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val sharedPref = applicationContext.getSharedPreferences("data", MODE_PRIVATE)
        val materialYou = sharedPref.getBoolean("material_you", true)
        val blackTheme = sharedPref.getBoolean("black_theme", false)
        val protected = sharedPref.getBoolean("protect_storage", false)
        setContent {
            OwnDroidTheme(materialYou, blackTheme) {
                AlertDialog(
                    title = {
                        Text(stringResource(R.string.clear_storage))
                    },
                    text = {
                        if(protected) Text(stringResource(R.string.storage_is_protected))
                    },
                    onDismissRequest = { finish() },
                    dismissButton = {
                        if(!protected) TextButton(onClick = { finish() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if(!protected) {
                                    applicationContext.filesDir.deleteRecursively()
                                    sharedPref.edit().clear().apply()
                                }
                                finish()
                            }
                        ) {
                            Text(stringResource(if(protected) R.string.cancel else R.string.confirm))
                        }
                    }
                )
            }
        }
    }
}