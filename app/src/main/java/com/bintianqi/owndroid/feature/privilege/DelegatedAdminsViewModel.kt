package com.bintianqi.owndroid.feature.privilege

import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.getAppInfo
import kotlinx.coroutines.flow.MutableStateFlow

class DelegatedAdminsViewModel(
    val application: MyApplication, val ph: PrivilegeHelper
) : ViewModel() {
    val delegatedAdminsState = MutableStateFlow(emptyList<DelegatedAdmin>())

    @RequiresApi(26)
    fun getDelegatedAdmins() = ph.safeDpmCall {
        val pm = application.packageManager
        val list = mutableListOf<DelegatedAdmin>()
        delegatedScopesList.forEach { scope ->
            dpm.getDelegatePackages(dar, scope.id)?.forEach { pkg ->
                val index = list.indexOfFirst { it.app.name == pkg }
                if (index == -1) {
                    list += DelegatedAdmin(getAppInfo(pm, pkg), listOf(scope.id))
                } else {
                    list[index] = DelegatedAdmin(list[index].app, list[index].scopes + scope.id)
                }
            }
        }
        delegatedAdminsState.value = list
    }

    var selectedDelegatedAdminIndex = -1

    @RequiresApi(26)
    fun setDelegatedAdmin(name: String, scopes: List<String>) = ph.safeDpmCall {
        dpm.setDelegatedScopes(dar, name, scopes)
        getDelegatedAdmins()
    }
}
