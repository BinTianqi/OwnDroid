package com.bintianqi.owndroid.feature.applications

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.utils.getAppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

class AppGroupViewModel(
    val application: MyApplication, val repo: AppGroupRepository,
    val appGroupsState: MutableStateFlow<List<AppGroup>>
) : ViewModel() {

    fun getAppGroups() {
        appGroupsState.value = repo.getAppGroups()
    }

    fun selectAppGroup(index: Int) {
        if (index == -1) {
            editorUiState.value = AppGroupEditorUiState()
        } else {
            val group = appGroupsState.value[index]
            val pm = application.packageManager
            editorUiState.value = AppGroupEditorUiState(
                group.id, group.name, group.apps.map { getAppInfo(pm, it) }
            )
        }
    }

    val editorUiState = MutableStateFlow(AppGroupEditorUiState())

    fun setGroupName(name: String) {
        editorUiState.update { it.copy(name = name) }
    }

    fun setGroupApp(name: String, state: Boolean) {
        editorUiState.update { uiState ->
            val newList = uiState.apps.let { list ->
                if (state) {
                    list.plus(getAppInfo(application.packageManager, name))
                } else {
                    list.filter { it.name != name }
                }
            }
            uiState.copy(apps = newList)
        }
    }

    fun setGroup() {
        val uiState = editorUiState.value
        repo.setAppGroup(uiState.id, uiState.name, uiState.apps.map { it.name })
        getAppGroups()
    }

    fun deleteGroup() {
        repo.deleteAppGroup(editorUiState.value.id!!)
        appGroupsState.update { group ->
            group.filter { it.id != editorUiState.value.id }
        }
    }

    fun exportGroups(uri: Uri) {
        application.contentResolver.openOutputStream(uri)!!.use {
            val list: List<BasicAppGroup> = appGroupsState.value
            it.write(Json.encodeToString(list).encodeToByteArray())
        }
    }

    fun importGroups(uri: Uri) {
        application.contentResolver.openInputStream(uri)!!.use {
            Json.decodeFromString<List<BasicAppGroup>>(it.readBytes().decodeToString())
        }.forEach {
            repo.setAppGroup(null, it.name, it.apps)
        }
        getAppGroups()
    }
}
