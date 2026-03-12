package com.bintianqi.owndroid.feature.users

import android.content.Context
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build.VERSION
import android.os.UserHandle
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ShortcutUtils
import com.bintianqi.owndroid.utils.ToastChannel
import com.bintianqi.owndroid.utils.doUserOperationWithContext
import com.bintianqi.owndroid.utils.plusOrMinus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsersViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val toastChannel: ToastChannel,
    val settingsRepo: SettingsRepository, val privilegeState: StateFlow<PrivilegeStatus>
) : ViewModel() {
    val um = application.getSystemService(Context.USER_SERVICE) as UserManager
    val logoutEnabledState = MutableStateFlow(false)

    @RequiresApi(28)
    fun getLogoutEnabled() = ph.safeDpmCall {
        logoutEnabledState.value = dpm.isLogoutEnabled
    }

    @RequiresApi(28)
    fun setLogoutEnabled(enabled: Boolean) = ph.safeDpmCall {
        dpm.setLogoutEnabled(dar, enabled)
        getLogoutEnabled()
    }

    val userInformationState = MutableStateFlow(UserInformation())
    fun getUserInformation() = ph.safeDpmCall {
        val uh = Binder.getCallingUserHandle()
        userInformationState.value = UserInformation(
            if (VERSION.SDK_INT >= 24) UserManager.supportsMultipleUsers() else false,
            if (VERSION.SDK_INT >= 31) UserManager.isHeadlessSystemUserMode() else false,
            um.isSystemUser,
            if (VERSION.SDK_INT >= 34) um.isAdminUser else false,
            if (VERSION.SDK_INT >= 25) um.isDemoUser else false,
            um.getUserCreationTime(uh),
            if (VERSION.SDK_INT >= 28) dpm.isLogoutEnabled else false,
            if (VERSION.SDK_INT >= 28) dpm.isEphemeralUser(dar) else false,
            if (VERSION.SDK_INT >= 28) dpm.isAffiliatedUser else false,
            um.getSerialNumberForUser(uh)
        )
    }

    val secondaryUsersState = MutableStateFlow(emptyList<UserIdentifier>())

    @Suppress("PrivateApi")
    @RequiresApi(28)
    fun getUserIdentifiers() = ph.safeDpmCall {
        secondaryUsersState.value = dpm.getSecondaryUsers(dar)?.mapNotNull {
            try {
                val field = UserHandle::class.java.getDeclaredField("mHandle")
                field.isAccessible = true
                UserIdentifier(field.get(it) as Int, um.getSerialNumberForUser(it))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } ?: emptyList()
    }

    fun doUserOperation(type: UserOperationType, id: Int, isUserId: Boolean) = ph.safeDpmCall {
        val result = doUserOperationWithContext(application, dpm, dar, type, id, isUserId)
        toastChannel.sendStatus(result)
    }

    fun createUserOperationShortcut(type: UserOperationType, id: Int, isUserId: Boolean): Boolean {
        val serial = if (isUserId && VERSION.SDK_INT >= 24) {
            um.getSerialNumberForUser(UserHandle.getUserHandleForUid(id * 100000))
        } else id
        return ShortcutUtils.setUserOperationShortcut(
            application, settingsRepo, type, serial.toInt()
        )
    }

    fun getUserOperationResultText(code: Int): Int {
        return when (code) {
            UserManager.USER_OPERATION_SUCCESS -> R.string.success
            UserManager.USER_OPERATION_ERROR_UNKNOWN -> R.string.unknown_error
            UserManager.USER_OPERATION_ERROR_MANAGED_PROFILE -> R.string.fail_managed_profile
            UserManager.USER_OPERATION_ERROR_MAX_RUNNING_USERS -> R.string.limit_reached
            UserManager.USER_OPERATION_ERROR_MAX_USERS -> R.string.limit_reached
            UserManager.USER_OPERATION_ERROR_CURRENT_USER -> R.string.fail_current_user
            else -> R.string.unknown
        }
    }

    @RequiresApi(24)
    fun createUser(
        name: String, flags: Int, callback: (CreateUserResult) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                ph.safeDpmCall {
                    val uh = dpm.createAndManageUser(dar, name, dar, null, flags)
                    if (uh == null) {
                        callback(CreateUserResult(R.string.failed))
                    } else {
                        callback(
                            CreateUserResult(R.string.succeeded, um.getSerialNumberForUser(uh))
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (VERSION.SDK_INT >= 28 && e is UserManager.UserOperationException) {
                    callback(CreateUserResult(getUserOperationResultText(e.userOperationResult)))
                } else {
                    callback(CreateUserResult(R.string.error))
                }
            }
        }
    }

    val affiliationIdsState = MutableStateFlow(emptyList<String>())

    @RequiresApi(26)
    fun getAffiliationIds() = ph.safeDpmCall {
        affiliationIdsState.value = dpm.getAffiliationIds(dar).toList()
    }

    fun setAffiliationId(id: String, state: Boolean) {
        affiliationIdsState.update { it.plusOrMinus(state, id) }
    }

    @RequiresApi(26)
    fun applyAffiliationIds() = ph.safeDpmCall {
        dpm.setAffiliationIds(dar, affiliationIdsState.value.toSet())
        toastChannel.sendStatus(true)
    }

    fun setProfileName(name: String) = ph.safeDpmCall {
        dpm.setProfileName(dar, name)
        toastChannel.sendStatus(true)
    }

    fun setUserIcon(bitmap: Bitmap) = ph.safeDpmCall {
        dpm.setUserIcon(dar, bitmap)
        toastChannel.sendStatus(true)
    }

    val startSessionMessageState = MutableStateFlow("")
    val endSessionMessageState = MutableStateFlow("")

    @RequiresApi(28)
    fun getSessionMessages() = ph.safeDpmCall {
        startSessionMessageState.value = dpm.getStartUserSessionMessage(dar)?.toString() ?: ""
        endSessionMessageState.value = dpm.getEndUserSessionMessage(dar)?.toString() ?: ""
    }

    fun setStartSessionMessage(message: String) {
        startSessionMessageState.value = message
    }

    fun setEndSessionMessage(message: String) {
        endSessionMessageState.value = message
    }

    @RequiresApi(28)
    fun applySessionMessages() = ph.safeDpmCall {
        dpm.setStartUserSessionMessage(dar, startSessionMessageState.value.ifEmpty { null })
        dpm.setEndUserSessionMessage(dar, endSessionMessageState.value.ifEmpty { null })
        toastChannel.sendStatus(true)
    }

    @RequiresApi(28)
    fun logout() = ph.safeDpmCall {
        val result = dpm.logoutUser(dar)
        if (result != 0) toastChannel.sendStatus(false)
    }
}
