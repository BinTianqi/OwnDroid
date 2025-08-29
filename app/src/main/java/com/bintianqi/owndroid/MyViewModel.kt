package com.bintianqi.owndroid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MyViewModel(application: Application): AndroidViewModel(application) {
    val theme = MutableStateFlow(ThemeSettings(SP.materialYou, SP.darkTheme, SP.blackTheme))
    fun changeTheme(newTheme: ThemeSettings) {
        theme.value = newTheme
        SP.materialYou = newTheme.materialYou
        SP.darkTheme = newTheme.darkTheme
        SP.blackTheme = newTheme.blackTheme
    }
}

data class ThemeSettings(
    val materialYou: Boolean = false,
    val darkTheme: Int = -1,
    val blackTheme: Boolean = false
)
