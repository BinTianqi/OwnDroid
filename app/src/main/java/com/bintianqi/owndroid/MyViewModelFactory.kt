package com.bintianqi.owndroid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bintianqi.owndroid.feature.applications.AppChooserViewModel
import com.bintianqi.owndroid.feature.applications.AppFeaturesViewModel
import com.bintianqi.owndroid.feature.applications.AppGroup
import com.bintianqi.owndroid.feature.applications.AppGroupRepository
import com.bintianqi.owndroid.feature.applications.AppGroupViewModel
import com.bintianqi.owndroid.feature.network.NetworkLoggingRepository
import com.bintianqi.owndroid.feature.network.NetworkLoggingViewModel
import com.bintianqi.owndroid.feature.network.NetworkStatsViewModel
import com.bintianqi.owndroid.feature.network.NetworkViewModel
import com.bintianqi.owndroid.feature.network.OverrideApnViewModel
import com.bintianqi.owndroid.feature.network.PreferentialNetworkViewModel
import com.bintianqi.owndroid.feature.network.WifiViewModel
import com.bintianqi.owndroid.feature.password.PasswordViewModel
import com.bintianqi.owndroid.feature.privilege.DelegatedAdminsViewModel
import com.bintianqi.owndroid.feature.privilege.DhizukuServerRepository
import com.bintianqi.owndroid.feature.privilege.DhizukuServerViewModel
import com.bintianqi.owndroid.feature.privilege.TransferOwnershipViewModel
import com.bintianqi.owndroid.feature.privilege.WorkingModesViewModel
import com.bintianqi.owndroid.feature.settings.MySettings
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.feature.settings.SettingsViewModel
import com.bintianqi.owndroid.feature.system.CaCertViewModel
import com.bintianqi.owndroid.feature.system.HardwareMonitorViewModel
import com.bintianqi.owndroid.feature.system.LockTaskModeViewModel
import com.bintianqi.owndroid.feature.system.SecurityLoggingRepository
import com.bintianqi.owndroid.feature.system.SecurityLoggingViewModel
import com.bintianqi.owndroid.feature.system.SystemOptionsViewModel
import com.bintianqi.owndroid.feature.system.SystemUpdateViewModel
import com.bintianqi.owndroid.feature.system.SystemViewModel
import com.bintianqi.owndroid.feature.system.TimeViewModel
import com.bintianqi.owndroid.feature.user_restriction.UserRestrictionViewModel
import com.bintianqi.owndroid.feature.users.UsersViewModel
import com.bintianqi.owndroid.feature.work_profile.CrossProfileIntentFilterRepository
import com.bintianqi.owndroid.feature.work_profile.CrossProfileIntentFilterViewModel
import com.bintianqi.owndroid.feature.work_profile.WorkProfileViewModel
import com.bintianqi.owndroid.utils.DhizukuError
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

class MyViewModelFactory(
    val app: MyApplication, val ph: PrivilegeHelper,
    val sr: SettingsRepository, val nlRepo: NetworkLoggingRepository,
    val dsRepo: DhizukuServerRepository, val slRepo: SecurityLoggingRepository,
    val agRepo: AppGroupRepository, val cpifRepo: CrossProfileIntentFilterRepository,
    val agState: MutableStateFlow<List<AppGroup>>,
    val de: MutableStateFlow<DhizukuError?>, val ps: MutableStateFlow<PrivilegeStatus>,
    val ts: MutableStateFlow<MySettings.Theme>, val tc: ToastChannel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        fun checkType(cls: KClass<*>): Boolean {
            return cls.java.isAssignableFrom(modelClass)
        }
        if (checkType(NetworkStatsViewModel::class)) {
            return NetworkStatsViewModel(app, ps) as T
        }
        if (checkType(WifiViewModel::class)) {
            return WifiViewModel(app, ph, tc, ps) as T
        }
        if (checkType(OverrideApnViewModel::class)) {
            return OverrideApnViewModel(ph, tc) as T
        }
        if (checkType(PreferentialNetworkViewModel::class)) {
            return PreferentialNetworkViewModel(ph) as T
        }
        if (checkType(NetworkLoggingViewModel::class)) {
            return NetworkLoggingViewModel(app, ph, nlRepo) as T
        }
        if (checkType(NetworkViewModel::class)) {
            return NetworkViewModel(ph, tc, ps) as T
        }

        if (checkType(DelegatedAdminsViewModel::class)) {
            return DelegatedAdminsViewModel(app, ph) as T
        }
        if (checkType(TransferOwnershipViewModel::class)) {
            return TransferOwnershipViewModel(app, ph, ps) as T
        }
        if (checkType(WorkingModesViewModel::class)) {
            return WorkingModesViewModel(app, ph, sr, ps, tc) as T
        }
        if (checkType(DhizukuServerViewModel::class)) {
            return DhizukuServerViewModel(app, dsRepo, sr) as T
        }

        if (checkType(SecurityLoggingViewModel::class)) {
            return SecurityLoggingViewModel(app, ph, slRepo, tc) as T
        }
        if (checkType(CaCertViewModel::class)) {
            return CaCertViewModel(app, ph, tc) as T
        }
        if (checkType(LockTaskModeViewModel::class)) {
            return LockTaskModeViewModel(app, ph, tc) as T
        }
        if (checkType(SystemOptionsViewModel::class)) {
            return SystemOptionsViewModel(app, ph, sr, ps) as T
        }
        if (checkType(SystemUpdateViewModel::class)) {
            return SystemUpdateViewModel(app, ph, tc) as T
        }
        if (checkType(HardwareMonitorViewModel::class)) {
            return HardwareMonitorViewModel(app) as T
        }
        if (checkType(TimeViewModel::class)) {
            return TimeViewModel(ph, tc) as T
        }
        if (checkType(SystemViewModel::class)) {
            return SystemViewModel(app, ph, sr, ps, tc) as T
        }

        if (checkType(AppGroupViewModel::class)) {
            return AppGroupViewModel(app, agRepo, agState) as T
        }
        if (checkType(AppFeaturesViewModel::class)) {
            return AppFeaturesViewModel(app, ph, ps, tc) as T
        }
        if (checkType(AppChooserViewModel::class)) {
            return AppChooserViewModel(app) as T
        }

        if (checkType(WorkProfileViewModel::class)) {
            return WorkProfileViewModel(ph, ps, tc) as T
        }
        if (checkType(CrossProfileIntentFilterViewModel::class)) {
            return CrossProfileIntentFilterViewModel(app, ph, cpifRepo, tc) as T
        }

        if (checkType(UserRestrictionViewModel::class)) {
            return UserRestrictionViewModel(app, ph, sr, ps, tc) as T
        }

        if (checkType(UsersViewModel::class)) {
            return UsersViewModel(app, ph, tc, sr, ps) as T
        }

        if (checkType(PasswordViewModel::class)) {
            return PasswordViewModel(app, ph, sr, ps, tc) as T
        }

        if (checkType(SettingsViewModel::class)) {
            return SettingsViewModel(app, sr, ph, ps, tc, ts) as T
        }
        throw Exception("Unknown ViewModel")
    }
}
