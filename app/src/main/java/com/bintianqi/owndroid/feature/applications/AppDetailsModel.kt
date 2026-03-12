package com.bintianqi.owndroid.feature.applications

data class AppDetailsUiState(
    val suspend: Boolean = false,
    val hide: Boolean = false,
    val uninstallBlocked: Boolean = false,
    val userControlDisabled: Boolean = false,
    val meteredDataDisabled: Boolean = false,
    val keepUninstalled: Boolean = false
)
