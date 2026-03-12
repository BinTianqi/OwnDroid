package com.bintianqi.owndroid

import com.bintianqi.owndroid.feature.applications.AppGroupRepository
import com.bintianqi.owndroid.feature.network.NetworkLoggingRepository
import com.bintianqi.owndroid.feature.privilege.DhizukuServerRepository
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.feature.system.SecurityLoggingRepository
import com.bintianqi.owndroid.feature.work_profile.CrossProfileIntentFilterRepository
import com.bintianqi.owndroid.utils.DhizukuError
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

class AppContainer(val app: MyApplication) {
    val dbHelper = MyDbHelper(app)
    val networkLoggingRepo = NetworkLoggingRepository(dbHelper)
    val securityLoggingRepo = SecurityLoggingRepository(dbHelper)
    val appGroupRepo = AppGroupRepository(dbHelper)
    val dhizukuServerRepo = DhizukuServerRepository(dbHelper)
    val cpifRepo = CrossProfileIntentFilterRepository(dbHelper)
    val settingsRepo = SettingsRepository(app.filesDir.resolve("settings.json"))
    val dhizukuErrorState = MutableStateFlow<DhizukuError?>(null)
    val privilegeHelper = PrivilegeHelper(
        app, settingsRepo.data.privilege.dhizuku, dhizukuErrorState
    )
    val privilegeState = MutableStateFlow(PrivilegeStatus())
    val appGroupsState = MutableStateFlow(appGroupRepo.getAppGroups())
    val chosenPackage = Channel<String>(1, BufferOverflow.DROP_LATEST)
    val themeState = MutableStateFlow(settingsRepo.data.theme)
    val toastChannel = ToastChannel(app)
    val viewModelFactory = MyViewModelFactory(
        app, privilegeHelper, settingsRepo, networkLoggingRepo, dhizukuServerRepo,
        securityLoggingRepo, appGroupRepo, cpifRepo, appGroupsState, dhizukuErrorState,
        privilegeState, themeState, toastChannel
    )
}
