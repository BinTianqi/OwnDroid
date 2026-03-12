package com.bintianqi.owndroid.feature.password

import android.app.admin.DevicePolicyManager
import android.os.Build.VERSION
import com.bintianqi.owndroid.R

class PasswordInfo(
    val complexity: Int = 0,
    val complexitySufficient: Boolean = false,
    val unified: Boolean = false
)

class RpTokenState(val set: Boolean = false, val active: Boolean = false)


class KeyguardDisabledFeature(val id: Int, val text: Int, val requiresApi: Int = 0)

@Suppress("InlinedApi")
val keyguardDisabledFeatures = listOf(
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL, R.string.disable_keyguard_features_widgets
    ),
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA,
        R.string.disable_keyguard_features_camera
    ),
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS,
        R.string.disable_keyguard_features_notification
    ),
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS,
        R.string.disable_keyguard_features_unredacted_notification
    ),
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS,
        R.string.disable_keyguard_features_trust_agents
    ),
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT,
        R.string.disable_keyguard_features_fingerprint
    ),
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_FACE, R.string.disable_keyguard_features_face, 28
    ),
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_IRIS, R.string.disable_keyguard_features_iris, 28
    ),
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS,
        R.string.disable_keyguard_features_biometrics, 28
    ),
    KeyguardDisabledFeature(
        DevicePolicyManager.KEYGUARD_DISABLE_SHORTCUTS_ALL,
        R.string.disable_keyguard_features_shortcuts, 34
    )
).filter { VERSION.SDK_INT >= it.requiresApi }

enum class KeyguardDisableMode(val text: Int) {
    None(R.string.enable_all), Custom(R.string.custom), All(R.string.disable_all)
}

data class KeyguardDisableConfig(
    val mode: KeyguardDisableMode = KeyguardDisableMode.None,
    val flags: Int = 0
)
