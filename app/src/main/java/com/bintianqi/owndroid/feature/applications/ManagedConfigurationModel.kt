package com.bintianqi.owndroid.feature.applications

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class AppRestriction(
    open val key: String, open val title: String?, open val description: String?
) {
    abstract fun isNull(): Boolean
    data class IntItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        var value: Int?,
    ) : AppRestriction(key, title, description) {
        override fun isNull(): Boolean = value == null
    }

    data class StringItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        var value: String?
    ) : AppRestriction(key, title, description) {
        override fun isNull(): Boolean = value == null
    }

    data class BooleanItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        var value: Boolean?
    ) : AppRestriction(key, title, description) {
        override fun isNull(): Boolean = value == null
    }

    data class ChoiceItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        val entries: Array<String>,
        val entryValues: Array<String>,
        var value: String?
    ) : AppRestriction(key, title, description) {
        override fun isNull(): Boolean = value == null
    }

    data class MultiSelectItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        val entries: Array<String>,
        val entryValues: Array<String>,
        var value: Array<String>?
    ) : AppRestriction(key, title, description) {
        override fun isNull(): Boolean = value == null
    }
}

data class MultiSelectEntry(val value: String, val title: String?, val selected: Boolean)

@Serializable
class AppRestrictionJson(
    val id: String,
    @SerialName("v_int") val vInt: Int? = null,
    @SerialName("v_str") val vString: String? = null, // for string and choice item
    @SerialName("v_bool") val vBool: Boolean? = null,
    @SerialName("v_list") val vList: List<String>? = null
)
