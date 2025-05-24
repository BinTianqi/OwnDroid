package com.bintianqi.owndroid

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPrefs(context: Context) {
    val sharedPrefs: SharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    var managedProfileActivated by BooleanSharedPref("managed_profile_activated")
    var dhizuku by BooleanSharedPref("dhizuku_mode")
    var isDefaultAffiliationIdSet by BooleanSharedPref("default_affiliation_id_set")
    var displayDangerousFeatures by BooleanSharedPref("display_dangerous_features")
    var isApiEnabled by BooleanSharedPref("api.enabled")
    var apiKey by StringSharedPref("api.key")
    var materialYou by BooleanSharedPref("theme.material_you", Build.VERSION.SDK_INT >= 31)
    /** -1: follow system, 0: off, 1: on */
    var darkTheme by IntSharedPref("theme.dark", -1)
    var blackTheme by BooleanSharedPref("theme.black")
    var lockPasswordHash by StringSharedPref("lock.password.sha256")
    var biometricsUnlock by BooleanSharedPref("lock.biometrics")
    var lockWhenLeaving by BooleanSharedPref("lock.onleave")
    var applicationsListView by BooleanSharedPref("applications.list_view", true)
    var shortcuts by BooleanSharedPref("shortcuts")
    var dhizukuServer by BooleanSharedPref("dhizuku_server")
}

private class BooleanSharedPref(val key: String, val defValue: Boolean = false): ReadWriteProperty<SharedPrefs, Boolean> {
    override fun getValue(thisRef: SharedPrefs, property: KProperty<*>): Boolean =
        thisRef.sharedPrefs.getBoolean(key, defValue)
    override fun setValue(thisRef: SharedPrefs, property: KProperty<*>, value: Boolean) =
        thisRef.sharedPrefs.edit { putBoolean(key, value) }
}

private class StringSharedPref(val key: String): ReadWriteProperty<SharedPrefs, String?> {
    override fun getValue(thisRef: SharedPrefs, property: KProperty<*>): String? =
        thisRef.sharedPrefs.getString(key, null)
    override fun setValue(thisRef: SharedPrefs, property: KProperty<*>, value: String?) =
        thisRef.sharedPrefs.edit { putString(key, value) }
}

private class IntSharedPref(val key: String, val defValue: Int = 0): ReadWriteProperty<SharedPrefs, Int> {
    override fun getValue(thisRef: SharedPrefs, property: KProperty<*>): Int =
        thisRef.sharedPrefs.getInt(key, defValue)
    override fun setValue(thisRef: SharedPrefs, property: KProperty<*>, value: Int) =
        thisRef.sharedPrefs.edit { putInt(key, value) }
}
