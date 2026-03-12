package com.bintianqi.owndroid.feature.work_profile

import android.content.Intent
import com.bintianqi.owndroid.R
import kotlinx.serialization.Serializable

@Serializable
data class IntentFilterOptions(
    val action: String, val category: String, val mimeType: String,
    val direction: Int // 1: private to work, 2: work to private, 3: both
)

val directionTextMap = mapOf(
    1 to R.string.personal_to_work,
    2 to R.string.work_to_personal,
    3 to R.string.both_direction
)

class IntentFilterPreset(
    val name: Int, val action: String, val category: String = "", val mimeType: String = ""
)

val crossProfileIntentFilterPresets = listOf(
    IntentFilterPreset(R.string.open_file, Intent.ACTION_VIEW, Intent.CATEGORY_DEFAULT, "*/*"),
    IntentFilterPreset(R.string.share, Intent.ACTION_SEND, Intent.CATEGORY_DEFAULT, "*/*"),
    IntentFilterPreset(R.string.share_multiple, Intent.ACTION_SEND_MULTIPLE),
    IntentFilterPreset(R.string.edit, Intent.ACTION_EDIT, Intent.CATEGORY_DEFAULT, "*/*"),
    IntentFilterPreset(R.string.get_content, Intent.ACTION_GET_CONTENT, Intent.CATEGORY_DEFAULT, "*/*"),
    IntentFilterPreset(R.string.install_app, Intent.ACTION_INSTALL_PACKAGE),
    IntentFilterPreset(R.string.uninstall_app, Intent.ACTION_UNINSTALL_PACKAGE),
    IntentFilterPreset(R.string.choose_file, Intent.ACTION_OPEN_DOCUMENT, Intent.CATEGORY_DEFAULT, "*/*"),
    IntentFilterPreset(R.string.choose_folder, Intent.ACTION_OPEN_DOCUMENT_TREE)
)
