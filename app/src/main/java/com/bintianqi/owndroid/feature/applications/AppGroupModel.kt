package com.bintianqi.owndroid.feature.applications

import com.bintianqi.owndroid.utils.AppInfo
import kotlinx.serialization.Serializable

@Serializable
open class BasicAppGroup(open val name: String, open val apps: List<String>)

class AppGroup(
    val id: Int, override val name: String, override val apps: List<String>
) : BasicAppGroup(name, apps)

data class AppGroupEditorUiState(
    val id: Int? = null, val name: String = "", val apps: List<AppInfo> = emptyList()
)
