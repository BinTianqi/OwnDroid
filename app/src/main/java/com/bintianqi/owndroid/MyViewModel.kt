package com.bintianqi.owndroid

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyViewModel(application: Application): AndroidViewModel(application) {
    val theme = MutableStateFlow(ThemeSettings())
    val installedPackages = mutableListOf<PackageInfo>()
    val selectedPackage = MutableStateFlow("")
    val userRestrictions = MutableStateFlow(Bundle())

    init {
        val sharedPrefs = application.getSharedPreferences("data", Context.MODE_PRIVATE)
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
