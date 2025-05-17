package com.bintianqi.owndroid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyViewModel(application: Application): AndroidViewModel(application) {
    val theme = MutableStateFlow(ThemeSettings())

    init {
        val sp = SharedPrefs(application)
        theme.value = ThemeSettings(sp.materialYou, sp.darkTheme, sp.blackTheme)
        viewModelScope.launch {
            theme.collect {
                sp.materialYou = it.materialYou
                sp.darkTheme = it.darkTheme
                sp.blackTheme = it.blackTheme
            }
        }
    }
}

data class ThemeSettings(
    val materialYou: Boolean = false,
    val darkTheme: Int = -1,
    val blackTheme: Boolean = false
)
