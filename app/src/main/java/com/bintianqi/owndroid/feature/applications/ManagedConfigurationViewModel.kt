package com.bintianqi.owndroid.feature.applications

import android.content.RestrictionEntry
import android.content.RestrictionsManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
    val manifestsState = MutableStateFlow(emptyList<AppRestrictionManifest>())
    val valuesState = MutableStateFlow(emptyList<AppRestrictionValue>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getRestrictionManifest()
        }
        viewModelScope.launch(Dispatchers.IO) {
            getRestrictionsWithoutCoroutine()
        }
    }

    private fun getRestrictionManifest() {
        try {
            val rm = application.getSystemService(RestrictionsManager::class.java)
            manifestsState.value = rm.getManifestRestrictions(packageName).mapNotNull {
                transformManifest(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getRestrictionsWithoutCoroutine() {
        ph.safeDpmCall {
            val bundle = dpm.getApplicationRestrictions(dar, packageName)
            valuesState.value = transformBundleToValues(bundle)
        }
    }

    fun setRestriction(item: AppRestrictionValue) {
        viewModelScope.launch(Dispatchers.IO) {
            ph.safeDpmCall {
                val bundle = transformValuesToBundle(
                    valuesState.value.filter { it.id != item.id } + item
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

    fun exportConfiguration(uri: Uri) {
        viewModelScope.launch {
            val json = Json {
                explicitNulls = false
            }
            val jsonBytes = json.encodeToString(valuesState.value).encodeToByteArray()
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
                    json.decodeFromString<List<AppRestrictionValue>>(
                        it.readBytes().decodeToString()
                    )
                }
                val bundle = transformValuesToBundle(list)
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
        const val TAG = "ManagedConfiguration"

        private fun transformManifest(
            entry: RestrictionEntry
        ): AppRestrictionManifest? {
            val type = when (entry.type) {
                RestrictionEntry.TYPE_INTEGER -> AppRestrictionType.Int
                RestrictionEntry.TYPE_STRING -> AppRestrictionType.String
                RestrictionEntry.TYPE_BOOLEAN -> AppRestrictionType.Boolean
                RestrictionEntry.TYPE_CHOICE -> AppRestrictionType.Choice
                RestrictionEntry.TYPE_MULTI_SELECT -> AppRestrictionType.MultiSelect
                else -> null
            }
            if (type == null) {
                Log.i(TAG, "Unsupported restriction \"${entry.key}\" with type ${entry.type}")
                return null
            }
            var entries: Array<String>? = null
            var entryValues: Array<String>? = null
            if (type == AppRestrictionType.Choice || type == AppRestrictionType.MultiSelect) {
                entries = entry.choiceEntries
                entryValues = entry.choiceValues
            }
            return AppRestrictionManifest(
                entry.key, type, entry.title, entry.description, entries, entryValues
            )
        }

        private fun transformValuesToBundle(values: List<AppRestrictionValue>): Bundle {
            val bundle = Bundle()
            values.forEach {
                if (it.vInt != null) {
                    bundle.putInt(it.id, it.vInt)
                } else if (it.vString != null) {
                    bundle.putString(it.id, it.vString)
                } else if (it.vBool != null) {
                    bundle.putBoolean(it.id, it.vBool)
                } else if (it.vList != null) {
                    bundle.putStringArray(it.id, it.vList.toTypedArray())
                }
            }
            return bundle
        }

        private fun transformBundleToValues(bundle: Bundle): List<AppRestrictionValue> {
            return bundle.keySet().mapNotNull { key ->
                when (val value = bundle.get(key)) {
                    is Int -> AppRestrictionValue(key, vInt = value)
                    is String -> AppRestrictionValue(key, vString = value)
                    is Boolean -> AppRestrictionValue(key, vBool = value)
                    is Array<*> -> {
                        if (value.all { it is String }) {
                            AppRestrictionValue(key, vList = (value as Array<String>).toList())
                        } else null
                    }
                    else -> null
                }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}
