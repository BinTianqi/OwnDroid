package com.bintianqi.owndroid.feature.applications

sealed class AppRestriction(
    open val key: String, open val title: String?, open val description: String?
) {
    data class IntItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        var value: Int?,
    ) : AppRestriction(key, title, description)
    data class StringItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        var value: String?
    ) : AppRestriction(key, title, description)
    data class BooleanItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        var value: Boolean?
    ) : AppRestriction(key, title, description)
    data class ChoiceItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        val entries: Array<String>,
        val entryValues: Array<String>,
        var value: String?
    ) : AppRestriction(key, title, description)
    data class MultiSelectItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        val entries: Array<String>,
        val entryValues: Array<String>,
        var value: Array<String>?
    ) : AppRestriction(key, title, description)
}

data class MultiSelectEntry(val value: String, val title: String?, val selected: Boolean)
