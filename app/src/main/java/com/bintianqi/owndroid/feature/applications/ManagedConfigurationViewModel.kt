package com.bintianqi.owndroid.feature.applications

import android.content.RestrictionEntry
import android.content.RestrictionsManager
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ManagedConfigurationViewModel(
    val packageName: String, val application: MyApplication, val ph: PrivilegeHelper,
    val toastChannel: ToastChannel
) : ViewModel() {
    val restrictionsState = MutableStateFlow(emptyList<AppRestriction>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getRestrictionsWithoutCoroutine()
        }
    }

    private fun getRestrictionsWithoutCoroutine() {
        try {
            val rm = application.getSystemService(RestrictionsManager::class.java)
            ph.safeDpmCall {
                val bundle = dpm.getApplicationRestrictions(dar, packageName)
                restrictionsState.value = rm.getManifestRestrictions(packageName)?.mapNotNull {
                    transformRestrictionEntry(it)
                }?.map {
                    if (bundle.containsKey(it.key)) {
                        when (it) {
                            is AppRestriction.BooleanItem -> it.value = bundle.getBoolean(it.key)
                            is AppRestriction.StringItem -> it.value = bundle.getString(it.key)
                            is AppRestriction.IntItem -> it.value = bundle.getInt(it.key)
                            is AppRestriction.ChoiceItem -> it.value = bundle.getString(it.key)
                            is AppRestriction.MultiSelectItem -> it.value =
                                bundle.getStringArray(it.key)
                        }
                    }
                    it
                } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setRestriction(item: AppRestriction) {
        viewModelScope.launch(Dispatchers.IO) {
            ph.safeDpmCall {
                val bundle = transformAppRestriction(
                    restrictionsState.value.filter { it.key != item.key }.plus(item)
                )
                dpm.setApplicationRestrictions(dar, packageName, bundle)
                getRestrictionsWithoutCoroutine()
            }
        }
    }

    fun clearRestrictions() {
        viewModelScope.launch(Dispatchers.IO) {
            ph.safeDpmCall {
                dpm.setApplicationRestrictions(dar, packageName, Bundle())
            }
            getRestrictionsWithoutCoroutine()
        }
    }

    private fun transformRestrictionEntry(e: RestrictionEntry): AppRestriction? {
        return when (e.type) {
            RestrictionEntry.TYPE_INTEGER ->
                AppRestriction.IntItem(e.key, e.title, e.description, null)

            RestrictionEntry.TYPE_STRING ->
                AppRestriction.StringItem(e.key, e.title, e.description, null)

            RestrictionEntry.TYPE_BOOLEAN ->
                AppRestriction.BooleanItem(e.key, e.title, e.description, null)

            RestrictionEntry.TYPE_CHOICE -> AppRestriction.ChoiceItem(
                e.key, e.title,
                e.description, e.choiceEntries, e.choiceValues, null
            )

            RestrictionEntry.TYPE_MULTI_SELECT -> AppRestriction.MultiSelectItem(
                e.key, e.title,
                e.description, e.choiceEntries, e.choiceValues, null
            )

            else -> null
        }
    }

    fun exportConfiguration(uri: Uri) {
        viewModelScope.launch {
            val list = restrictionsState.value.map {
                when (it) {
                    is AppRestriction.IntItem -> AppRestrictionJson(it.key, vInt = it.value)
                    is AppRestriction.StringItem -> AppRestrictionJson(it.key, vString = it.value)
                    is AppRestriction.BooleanItem -> AppRestrictionJson(it.key, vBool = it.value)
                    is AppRestriction.ChoiceItem -> AppRestrictionJson(it.key, vString = it.value)
                    is AppRestriction.MultiSelectItem -> AppRestrictionJson(
                        it.key, vList = it.value?.toList()
                    )
                }
            }
            val json = Json {
                explicitNulls = false
            }
            val jsonBytes = json.encodeToString(list).encodeToByteArray()
            application.contentResolver.openOutputStream(uri)?.use {
                it.write(jsonBytes)
            }
            toastChannel.sendStatus(true)
        }
    }

    fun importConfiguration(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = Json {
                    explicitNulls = false
                }
                val list = application.contentResolver.openInputStream(uri)!!.use {
                    json.decodeFromString<List<AppRestrictionJson>>(it.readBytes().decodeToString())
                }
                restrictionsState.value.forEach { restriction ->
                    val item = list.find { it.id == restriction.key }
                    when (restriction) {
                        is AppRestriction.IntItem -> restriction.value = item?.vInt
                        is AppRestriction.StringItem -> restriction.value = item?.vString
                        is AppRestriction.BooleanItem -> restriction.value = item?.vBool
                        is AppRestriction.ChoiceItem -> {
                            if (item?.vString in restriction.entryValues) {
                                restriction.value = item?.vString
                            }
                        }
                        is AppRestriction.MultiSelectItem -> {
                            restriction.value = item?.vList?.filter {
                                it in restriction.entryValues
                            }?.toTypedArray()
                        }
                    }
                }
                val bundle = transformAppRestriction(restrictionsState.value)
                ph.safeDpmCall {
                    dpm.setApplicationRestrictions(dar, packageName, bundle)
                }
                getRestrictionsWithoutCoroutine()
                toastChannel.sendStatus(true)
            } catch (_: Exception) {
                toastChannel.sendStatus(false)
            }
        }
    }

    companion object {
        private fun transformAppRestriction(list: List<AppRestriction>): Bundle {
            val b = Bundle()
            for (r in list) {
                when (r) {
                    is AppRestriction.IntItem -> r.value?.let { b.putInt(r.key, it) }
                    is AppRestriction.StringItem -> r.value?.let { b.putString(r.key, it) }
                    is AppRestriction.BooleanItem -> r.value?.let { b.putBoolean(r.key, it) }
                    is AppRestriction.ChoiceItem -> r.value?.let { b.putString(r.key, it) }
                    is AppRestriction.MultiSelectItem -> r.value?.let {
                        b.putStringArray(r.key, r.value)
                    }
                }
            }
            return b
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}
