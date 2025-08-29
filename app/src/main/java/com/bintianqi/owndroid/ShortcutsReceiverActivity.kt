package com.bintianqi.owndroid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

class ShortcutsReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val action = intent.action?.removePrefix("com.bintianqi.owndroid.action.")
            if (action != null && SP.shortcuts) {
                when (action) {
                    "LOCK" -> Privilege.DPM.lockNow()
                    "DISABLE_CAMERA" -> {
                        Privilege.DPM.setCameraDisabled(Privilege.DAR, !Privilege.DPM.getCameraDisabled(Privilege.DAR))
                        createShortcuts(this)
                    }
                    "MUTE" -> {
                        Privilege.DPM.setMasterVolumeMuted(Privilege.DAR, !Privilege.DPM.isMasterVolumeMuted(Privilege.DAR))
                        createShortcuts(this)
                    }
                }
            }
        } finally {
            finish()
        }
    }
}

fun createShortcuts(context: Context) {
    if (!SP.shortcuts) return
    val action = "com.bintianqi.owndroid.action"
    val baseIntent = Intent(context, ShortcutsReceiverActivity::class.java)
    val cameraDisabled = Privilege.DPM.getCameraDisabled(Privilege.DAR)
    val muted = Privilege.DPM.isMasterVolumeMuted(Privilege.DAR)
    val list = listOf(
        ShortcutInfoCompat.Builder(context, "LOCK")
            .setIcon(IconCompat.createWithResource(context, R.drawable.screen_lock_portrait_fill0))
            .setShortLabel(context.getString(R.string.lock_screen))
            .setIntent(Intent(baseIntent).setAction("$action.LOCK")),
        ShortcutInfoCompat.Builder(context, "DISABLE_CAMERA")
            .setIcon(
                IconCompat.createWithResource(
                    context,
                    if (cameraDisabled) R.drawable.photo_camera_fill0 else R.drawable.no_photography_fill0
                )
            )
            .setShortLabel(context.getString(if (cameraDisabled) R.string.enable_camera else R.string.disable_cam))
            .setIntent(Intent(baseIntent).setAction("$action.DISABLE_CAMERA")),
        ShortcutInfoCompat.Builder(context, "MUTE")
            .setIcon(
                IconCompat.createWithResource(
                    context,
                    if (muted) R.drawable.volume_up_fill0 else R.drawable.volume_off_fill0
                )
            )
            .setShortLabel(context.getString(if (muted) R.string.unmute else R.string.mute))
            .setIntent(Intent(baseIntent).setAction("$action.MUTE"))
    )
    ShortcutManagerCompat.setDynamicShortcuts(context, list.map { it.build() })
}
