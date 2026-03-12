package com.bintianqi.owndroid.feature.user_restriction

import android.content.Context
import android.os.UserManager
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ShortcutUtils
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class UserRestrictionViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val settingsRepo: SettingsRepository,
    val privilegeState: StateFlow<PrivilegeStatus>, val toastChannel: ToastChannel
) : ViewModel() {
    val restrictionsState = MutableStateFlow(emptyMap<String, Boolean>())

    fun getRestrictions() {
        val um = application.getSystemService(Context.USER_SERVICE) as UserManager
        val bundle = um.userRestrictions
        restrictionsState.value = bundle.keySet().associateWith { bundle.getBoolean(it) }
    }

    fun setRestriction(name: String, state: Boolean) = ph.safeDpmCall {
        val result = try {
            if (state) {
                dpm.addUserRestriction(dar, name)
            } else {
                dpm.clearUserRestriction(dar, name)
            }
            restrictionsState.update { it.plus(name to state) }
            getRestrictions()
            ShortcutUtils.updateUserRestrictionShortcut(
                application, settingsRepo, name, !state, true
            )
            true
        } catch (_: SecurityException) {
            false
        }
        if (!result) toastChannel.sendStatus(false)
    }

    fun createShortcut(id: String) {
        val result = ShortcutUtils.setUserRestrictionShortcut(
            application, settingsRepo, id, restrictionsState.value[id] ?: true
        )
        if (!result) toastChannel.sendText(R.string.unsupported)
    }
}
