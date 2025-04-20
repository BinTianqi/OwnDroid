package com.bintianqi.owndroid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bintianqi.owndroid.dpm.getDPM
import com.bintianqi.owndroid.dpm.getReceiver

class ShortcutsReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val action = intent.action?.removePrefix("com.bintianqi.owndroid.action.")
            if (action != null && SharedPrefs(this).shortcuts) {
                val dpm = getDPM()
                val receiver = getReceiver()
                when (action) {
                    "LOCK" -> dpm.lockNow()
                    "DISABLE_CAMERA" -> {
                        dpm.setCameraDisabled(receiver, !dpm.getCameraDisabled(receiver))
                        createShortcuts(this)
                    }
                    "MUTE" -> {
                        dpm.setMasterVolumeMuted(receiver, !dpm.isMasterVolumeMuted(receiver))
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
    if (!SharedPrefs(context).shortcuts) return
    val action = "com.bintianqi.owndroid.action"
    val baseIntent = Intent(context, ShortcutsReceiverActivity::class.java)
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val cameraDisabled = dpm.getCameraDisabled(receiver)
    val muted = dpm.isMasterVolumeMuted(receiver)
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
