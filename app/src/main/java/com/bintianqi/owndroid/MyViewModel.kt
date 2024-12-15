package com.bintianqi.owndroid

import android.content.Context
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyViewModel: ViewModel() {
    val theme = MutableStateFlow(ThemeSettings())
    val shizukuBinder = MutableStateFlow<IBinder?>(null)

    var initialized = false
    fun initialize(context: Context) {
        val sharedPrefs = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        theme.value = ThemeSettings(
            materialYou = sharedPrefs.getBoolean("material_you", Build.VERSION.SDK_INT >= 31),
            darkTheme = if(sharedPrefs.contains("dark_theme")) sharedPrefs.getBoolean("dark_theme", false) else null,
            blackTheme = sharedPrefs.getBoolean("black_theme", false)
        )
        viewModelScope.launch {
            theme.collect {
                val editor = sharedPrefs.edit()
                editor.putBoolean("material_you", it.materialYou)
                if(it.darkTheme == null) editor.remove("dark_theme") else editor.putBoolean("dark_theme", it.darkTheme)
                editor.putBoolean("black_theme", it.blackTheme)
                editor.commit()
            }
        }
    }
}

data class ThemeSettings(
    val materialYou: Boolean = false,
    val darkTheme: Boolean? = null,
    val blackTheme: Boolean = false
)
