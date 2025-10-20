package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bintianqi.owndroid.dpm.UserOperationType

object ShortcutUtils {
    fun setAllShortcuts(context: Context, enabled: Boolean) {
        if (enabled) {
            setShortcutKey()
            val list = listOf(
                createShortcut(context, MyShortcut.Lock, true),
                createShortcut(context, MyShortcut.DisableCamera,
                    !Privilege.DPM.getCameraDisabled(Privilege.DAR)),
                createShortcut(context, MyShortcut.Mute,
                    !Privilege.DPM.isMasterVolumeMuted(Privilege.DAR))
            )
            ShortcutManagerCompat.setDynamicShortcuts(context, list)
        } else {
            ShortcutManagerCompat.removeDynamicShortcuts(context, MyShortcut.entries.map { it.id })
        }
    }
    fun setShortcut(context: Context, shortcut: MyShortcut, state: Boolean) {
        setShortcutKey()
        ShortcutManagerCompat.pushDynamicShortcut(
            context, createShortcut(context, shortcut, state)
        )
    }
    private fun createShortcut(
        context: Context, shortcut: MyShortcut, state: Boolean
    ): ShortcutInfoCompat {
        val icon = IconCompat.createWithResource(
            context,
            if (!state && shortcut.iconDisable != null) shortcut.iconDisable else shortcut.iconEnable
        )
        return ShortcutInfoCompat.Builder(context, shortcut.id)
            .setIcon(icon)
            .setShortLabel(context.getText(
                if (!state && shortcut.labelDisable != null) shortcut.labelDisable else shortcut.labelEnable
            ))
            .setIntent(
                Intent(context, ShortcutsReceiverActivity::class.java)
                    .setAction("com.bintianqi.owndroid.action.${shortcut.id}")
                    .putExtra("key", SP.shortcutKey)
            )
            .build()
    }
    /** @param state If true, set the user restriction */
    fun createUserRestrictionShortcut(context: Context, id: String, state: Boolean): ShortcutInfoCompat {
        val restriction = UserRestrictionsRepository.findRestrictionById(id)
        val label = context.getString(if (state) R.string.disable else R.string.enable) + " " +
                context.getString(restriction.name)
        setShortcutKey()
        return ShortcutInfoCompat.Builder(context, "USER_RESTRICTION-$id")
            .setIcon(IconCompat.createWithResource(context, restriction.icon))
            .setShortLabel(label)
            .setIntent(
                Intent(context, ShortcutsReceiverActivity::class.java)
                    .setAction("com.bintianqi.owndroid.action.USER_RESTRICTION")
                    .putExtra("restriction", id)
                    .putExtra("state", state)
                    .putExtra("key", SP.shortcutKey)
            )
            .build()
    }
    fun setUserRestrictionShortcut(context: Context, id: String, state: Boolean): Boolean {
        val shortcut = createUserRestrictionShortcut(context, id, state)
        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }
    fun updateUserRestrictionShortcut(context: Context, id: String, state: Boolean, checkExist: Boolean) {
        if (checkExist) {
            val shortcuts = ShortcutManagerCompat.getShortcuts(
                context, ShortcutManagerCompat.FLAG_MATCH_PINNED
            )
            if (shortcuts.find { it.id == "USER_RESTRICTION-$id" } == null) return
        }
        val shortcut = createUserRestrictionShortcut(context, id, state)
        ShortcutManagerCompat.updateShortcuts(context, listOf(shortcut))
    }
    fun buildUserOperationShortcut(
        context: Context, type: UserOperationType, serial: Int
    ): ShortcutInfoCompat {
        setShortcutKey()
        val icon = when (type) {
            UserOperationType.Start, UserOperationType.Switch -> R.drawable.person_fill0
            UserOperationType.Stop -> R.drawable.person_off
            else -> R.drawable.person_fill0
        }
        val text = when (type) {
            UserOperationType.Start -> R.string.start_user_n
            UserOperationType.Switch -> R.string.switch_to_user_n
            UserOperationType.Stop -> R.string.stop_user_n
            else -> R.string.place_holder
        }
        return ShortcutInfoCompat.Builder(context, "USER_OPERATION-${type.name}-$serial")
            .setIcon(IconCompat.createWithResource(context, icon))
            .setShortLabel(context.getString(text, serial))
            .setIntent(
                Intent(context, ShortcutsReceiverActivity::class.java)
                    .setAction("com.bintianqi.owndroid.action.USER_OPERATION")
                    .putExtra("operation", type.name)
                    .putExtra("serial", serial)
                    .putExtra("key", SP.shortcutKey)
            )
            .build()
    }
    fun setUserOperationShortcut(context: Context, type: UserOperationType, serial: Int): Boolean {
        val shortcut = buildUserOperationShortcut(context, type, serial)
        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }
    fun deleteUserOperationShortcut(context: Context, serial: Int) {
        val shortcuts = ShortcutManagerCompat.getShortcuts(
            context, ShortcutManagerCompat.FLAG_MATCH_PINNED
        )
        val matchedShortcuts = shortcuts.filter {
            it.id.startsWith("USER_OPERATION-") && it.id.endsWith("-$serial")
        }.map { it.id }
        ShortcutManagerCompat.removeLongLivedShortcuts(context, matchedShortcuts)
    }
    fun setShortcutKey() {
        if (SP.shortcutKey.isNullOrEmpty()) {
            SP.shortcutKey = generateBase64Key(10)
        }
    }
}

enum class MyShortcut(
    val id: String, val labelEnable: Int, val labelDisable: Int? = null, val iconEnable: Int,
    val iconDisable: Int? = null
) {
    Lock("LOCK", R.string.lock_screen, iconEnable = R.drawable.lock_fill0),
    DisableCamera("DISABLE_CAMERA", R.string.disable_cam, R.string.enable_camera,
        R.drawable.no_photography_fill0, R.drawable.photo_camera_fill0),
    Mute("MUTE", R.string.mute, R.string.unmute, R.drawable.volume_off_fill0, R.drawable.volume_up_fill0)
}