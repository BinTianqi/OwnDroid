package com.bintianqi.owndroid.feature.applications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Don't modify their names because they're used for serialization
 */
enum class AppRestrictionType {
    Int, String, Boolean, Choice, MultiSelect
}

class AppRestrictionManifest(
    val key: String, val type: AppRestrictionType,
    val title: String?, val description: String?,
    val entries: Array<String>? = null, val entryValues: Array<String>? = null
)

@Serializable
class AppRestrictionValue(
    val id: String,
    @SerialName("v_int") val vInt: Int? = null,
    @SerialName("v_str") val vString: String? = null, // for string and choice item
    @SerialName("v_bool") val vBool: Boolean? = null,
    @SerialName("v_list") val vList: List<String>? = null
)
