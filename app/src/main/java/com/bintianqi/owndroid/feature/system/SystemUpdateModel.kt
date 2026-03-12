package com.bintianqi.owndroid.feature.system

import android.app.admin.SystemUpdatePolicy
import com.bintianqi.owndroid.R

data class SystemUpdatePolicyUiState(
    val type: SystemUpdatePolicyType = SystemUpdatePolicyType.None,
    val start: String = "",
    val end: String = ""
)

enum class SystemUpdatePolicyType(val id: Int, val text: Int) {
    None(-1, R.string.none),
    Automatic(SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC, R.string.automatic),
    Windowed(SystemUpdatePolicy.TYPE_INSTALL_WINDOWED, R.string.system_update_policy_windowed),
    Postpone(SystemUpdatePolicy.TYPE_POSTPONE, R.string.system_update_policy_postpone)
}

class PendingSystemUpdateInfo(
    val exists: Boolean = false,
    val time: Long = 0,
    val securityPatch: Boolean = false
)
