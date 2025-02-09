package com.bintianqi.owndroid

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPrefs(context: Context) {
    val sharedPrefs = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    var managedProfileActivated by BooleanSharedPref("managed_profile_activated")
    var dhizuku by BooleanSharedPref("dhizuku_mode")
    var isDefaultAffiliationIdSet by BooleanSharedPref("default_affiliation_id_set")
    var displayDangerousFeatures by BooleanSharedPref("display_dangerous_features")
    var isApiEnabled by BooleanSharedPref("api.enabled")
    var apiKey by StringSharedPref("api.key")
    var auth by BooleanSharedPref("auth")
    var biometricsAuth by IntSharedPref("auth.biometrics")
    var lockInBackground by BooleanSharedPref("auth.lock_in_background")
    var materialYou by BooleanSharedPref("theme.material_you", Build.VERSION.SDK_INT >= 31)
    /** -1: follow system, 0: off, 1: on */
    var darkTheme by IntSharedPref("theme.dark", -1)
    var blackTheme by BooleanSharedPref("theme.black")
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
