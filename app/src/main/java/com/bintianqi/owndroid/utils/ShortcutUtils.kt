package com.bintianqi.owndroid.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ShortcutsReceiverActivity
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.feature.user_restriction.UserRestrictionsRepository
import com.bintianqi.owndroid.feature.users.UserOperationType

object ShortcutUtils {
    fun getShortcuts(context: Context): List<Shortcut> {
        return ShortcutManagerCompat.getShortcuts(
            context, ShortcutManagerCompat.FLAG_MATCH_PINNED
        ).map { shortcut ->
            Shortcut(
                shortcut.id,
                shortcut.shortLabel.toString(),
                shortcut.isEnabled,
                ShortcutAction.entries.find {
                    it.name == shortcut.intent.getStringExtra("action")
                }!!,
                ShortcutSystemOption.entries.find {
                    it.name == shortcut.intent.getStringExtra("option")
                },
                shortcut.intent.getStringExtra("restriction"),
                shortcut.intent.getBooleanExtra("state", false),
                UserOperationType.entries.find {
                    it.name == shortcut.intent.getStringExtra("operation")
                },
                shortcut.intent.getIntExtra("serial", 0)
            )
        }
    }

    /** @param state `true` means the option is currently enabled */
    private fun buildSystemOptionShortcut(
        context: Context, id: String, sr: SettingsRepository,
        option: ShortcutSystemOption, state: Boolean
    ): ShortcutInfoCompat {
        val icon = if (state) option.disableIcon else option.enableIcon
        val label = context.getString(if (state) R.string.disable else R.string.enable) + " " +
                context.getString(option.label)
        return ShortcutInfoCompat.Builder(context, id)
            .setIcon(IconCompat.createWithResource(context, icon))
            .setShortLabel(label)
            .setIntent(
                getBaseIntent(context, sr)
                    .putExtra("action", ShortcutAction.SystemOption.name)
                    .putExtra("option", option.name)
                    .putExtra("state", state)
            )
            .build()
    }

    fun pinSystemOptionShortcut(
        context: Context, sr: SettingsRepository, option: ShortcutSystemOption, state: Boolean
    ) : Boolean{
        val shortcut = buildSystemOptionShortcut(
            context, generateIncrementalId(sr), sr, option, state
        )
        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun updateSystemOptionShortcuts(
        context: Context, sr: SettingsRepository, option: ShortcutSystemOption, state: Boolean
    ) {
        val shortcuts = getShortcuts(context).filter {
            it.action == ShortcutAction.SystemOption && it.option == option
        }.map {
            buildSystemOptionShortcut(context, it.id, sr, option, state)
        }
        ShortcutManagerCompat.updateShortcuts(context, shortcuts)
    }

    /** @param state `true` means the user restriction is currently enabled */
    private fun buildUserRestrictionShortcut(
        id: String, context: Context, sr: SettingsRepository, restrictionId: String, state: Boolean
    ): ShortcutInfoCompat {
        val restriction = UserRestrictionsRepository.findRestrictionById(restrictionId)
        // Enabling a user restriction is to disable that function
        val label = context.getString(if (state) R.string.disable else R.string.enable) + " " +
                context.getString(restriction.name)
        return ShortcutInfoCompat.Builder(context, id)
            .setIcon(IconCompat.createWithResource(context, restriction.icon))
            .setShortLabel(label)
            .setIntent(
                getBaseIntent(context, sr)
                    .putExtra("action", ShortcutAction.UserRestriction.name)
                    .putExtra("restriction", restrictionId)
                    .putExtra("state", state)
            )
            .build()
    }

    fun pinUserRestrictionShortcut(
        context: Context, sr: SettingsRepository, restrictionId: String, state: Boolean
    ): Boolean {
        val shortcut = buildUserRestrictionShortcut(
            generateIncrementalId(sr), context, sr, restrictionId, state
        )
        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun updateUserRestrictionShortcuts(
        context: Context, sr: SettingsRepository, restriction: String, state: Boolean
    ) {
        val shortcuts = getShortcuts(context).filter {
            it.action == ShortcutAction.UserRestriction && it.restriction == restriction
        }.map {
            buildUserRestrictionShortcut(it.id, context, sr, restriction, state)
        }
        ShortcutManagerCompat.updateShortcuts(context, shortcuts)
    }

    fun pinUserOperationShortcut(
        context: Context, sr: SettingsRepository, type: UserOperationType, serial: Int
    ): Boolean {
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
        val shortcut = ShortcutInfoCompat.Builder(context, generateIncrementalId(sr))
            .setIcon(IconCompat.createWithResource(context, icon))
            .setShortLabel(context.getString(text, serial))
            .setIntent(
                getBaseIntent(context, sr)
                    .putExtra("action", ShortcutAction.UserOperation.name)
                    .putExtra("operation", type.name)
                    .putExtra("serial", serial)
            )
            .build()
        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun disableUserOperationShortcuts(context: Context, serial: Int) {
        val shortcuts = getShortcuts(context).filter {
            it.action == ShortcutAction.UserOperation && it.serial == serial
        }
        ShortcutManagerCompat.disableShortcuts(
            context, shortcuts.map { it.id }, context.getString(R.string.user_removed)
        )
    }

    fun pinLogoutShortcut(context: Context, sr: SettingsRepository): Boolean {
        val shortcut = ShortcutInfoCompat.Builder(context, generateIncrementalId(sr))
            .setIcon(IconCompat.createWithResource(context, R.drawable.logout_fill0))
            .setShortLabel(context.getText(R.string.logout))
            .setIntent(
                getBaseIntent(context, sr)
                    .putExtra("action", ShortcutAction.Logout.name)
            )
            .build()
        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun pinLockScreenShortcut(context: Context, sr: SettingsRepository): Boolean {
        val shortcut = ShortcutInfoCompat.Builder(context, generateIncrementalId(sr))
            .setIcon(IconCompat.createWithResource(context, R.drawable.screen_lock_portrait_fill0))
            .setShortLabel(context.getText(R.string.lock_screen))
            .setIntent(
                getBaseIntent(context, sr)
                    .putExtra("action", ShortcutAction.LockScreen.name)
            )
            .build()
        return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }

    fun setSingleShortcutEnabled(context: Context, id: String, enabled: Boolean) {
        if (enabled) {
            val shortcuts = ShortcutManagerCompat.getShortcuts(
                context, ShortcutManagerCompat.FLAG_MATCH_PINNED
            ).filter { it.id == id }
            ShortcutManagerCompat.enableShortcuts(context, shortcuts)
        } else {
            ShortcutManagerCompat.disableShortcuts(context, listOf(id), null)
        }
    }

    private fun getBaseIntent(context: Context, sr: SettingsRepository): Intent {
        return Intent(context, ShortcutsReceiverActivity::class.java)
            .setAction(Intent.ACTION_DEFAULT)
            .putExtra("key", getShortcutKey(sr))
    }

    private fun getShortcutKey(sr: SettingsRepository): String {
        var key = sr.data.shortcut.key
        if (key.isEmpty()) {
            key = generateBase64Key(10)
            sr.update {
                it.shortcut.key = key
            }
        }
        return key
    }

    private fun generateIncrementalId(sr: SettingsRepository): String {
        val id = sr.data.shortcut.id
        sr.update { it.shortcut.id += 1 }
        return id.toString()
    }
}

enum class ShortcutAction {
    UserRestriction, UserOperation, SystemOption, LockScreen, Logout
}

enum class ShortcutSystemOption(val label: Int, val enableIcon: Int, val disableIcon: Int) {
    Camera(R.string.camera, R.drawable.photo_camera_fill0, R.drawable.no_photography_fill0),
    Mute(R.string.mute, R.drawable.volume_off_fill0, R.drawable.volume_up_fill0)
}

class Shortcut(
    val id: String,
    val label: String,
    val enabled: Boolean,
    val action: ShortcutAction,
    val option: ShortcutSystemOption?,
    val restriction: String?, // User restriction
    val state: Boolean, // Current user restriction / system option state
    val operation: UserOperationType?,
    val serial: Int, // User operation target user
) {
    fun getIcon() : Int {
        return when (action) {
            ShortcutAction.UserRestriction -> {
                UserRestrictionsRepository.findRestrictionById(restriction!!).icon
            }
            ShortcutAction.UserOperation -> when (operation) {
                UserOperationType.Start, UserOperationType.Switch -> R.drawable.person_fill0
                UserOperationType.Stop -> R.drawable.person_off
                else -> R.drawable.person_fill0
            }

            ShortcutAction.SystemOption -> {
                if (state) option!!.disableIcon else option!!.enableIcon
            }

            ShortcutAction.LockScreen -> R.drawable.screen_lock_portrait_fill0
            ShortcutAction.Logout -> R.drawable.logout_fill0
        }
    }
}
