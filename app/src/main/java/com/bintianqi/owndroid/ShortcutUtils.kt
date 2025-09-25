package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

object ShortcutUtils {
    fun setAllShortcuts(context: Context) {
        if (SP.shortcuts) {
            val list = listOf(
                createShortcut(context, MyShortcut.Lock, true),
                createShortcut(context, MyShortcut.DisableCamera,
                    !Privilege.DPM.getCameraDisabled(Privilege.DAR)),
                createShortcut(context, MyShortcut.Mute,
                    !Privilege.DPM.isMasterVolumeMuted(Privilege.DAR))
            )
            ShortcutManagerCompat.setDynamicShortcuts(context, list)
        } else {
            ShortcutManagerCompat.removeAllDynamicShortcuts(context)
        }
    }
    fun setShortcut(context: Context, shortcut: MyShortcut, state: Boolean) {
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
            )
            .build()
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