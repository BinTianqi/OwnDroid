package com.bintianqi.owndroid.utils

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.encoding.Base64

fun uriToStream(
    context: Context,
    uri: Uri,
    operation: (stream: InputStream)->Unit
){
    try {
        context.contentResolver.openInputStream(uri)?.use {
            operation(it)
        }
    }
    catch(_: FileNotFoundException) { context.popToast(R.string.file_not_exist) }
    catch(_: IOException) { context.popToast(R.string.io_exception) }
}

fun writeClipBoard(context: Context, string: String):Boolean{
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    try {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", string))
    } catch(_:Exception) {
        return false
    }
    return true
}

fun formatFileSize(bytes: Long): String {
    val kb = 1024
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        bytes >= gb -> String.format(Locale.US, "%.2f GB", bytes / gb.toDouble())
        bytes >= mb -> String.format(Locale.US, "%.2f MB", bytes / mb.toDouble())
        bytes >= kb -> String.format(Locale.US, "%.2f KB", bytes / kb.toDouble())
        else -> "$bytes bytes"
    }
}

val Boolean.yesOrNo
    get() = if(this) R.string.yes else R.string.no

fun formatDate(ms: Long): String {
    return formatDate(Date(ms))
}
fun formatDate(date: Date): String {
    return SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(date)
}

fun Context.showOperationResultToast(success: Boolean) {
    popToast(if(success) R.string.success else R.string.failed)
}

const val APK_MIME = "application/vnd.android.package-archive"

val HorizontalPadding = 16.dp

val BottomPadding = 60.dp

@OptIn(ExperimentalStdlibApi::class)
fun String.hash(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return md.digest(this.encodeToByteArray()).toHexString()
}

val MyAdminComponent = ComponentName.unflattenFromString("com.bintianqi.owndroid/.Receiver")!!


@OptIn(ExperimentalStdlibApi::class)
fun getPackageSignature(info: PackageInfo): String? {
    val signatures = if (VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
    return signatures?.firstOrNull()?.toByteArray()
        ?.let { MessageDigest.getInstance("SHA-256").digest(it) }?.toHexString()
}

fun Context.popToast(resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}

fun Context.popToast(str: String) {
    Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
}

class SerializableSaver<T>(val serializer: KSerializer<T>) : Saver<T, String> {
    override fun restore(value: String): T? {
        return Json.decodeFromString(serializer, value)
    }
    override fun SaverScope.save(value: T): String {
        return Json.encodeToString(serializer, value)
    }
}

fun generateBase64Key(length: Int): String {
    val ba = ByteArray(length)
    SecureRandom().nextBytes(ba)
    return Base64.withPadding(Base64.PaddingOption.ABSENT).encode(ba)
}

fun Modifier.clickableTextField(onClick: () -> Unit) =
    pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(pass = PointerEventPass.Initial)
            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
            if (upEvent != null) onClick()
        }
    }

@Composable
fun adaptiveInsets(): WindowInsets {
    val navbar = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
    return WindowInsets.ime.union(navbar).union(WindowInsets.displayCutout)
}

fun registerPackageRemovedReceiver(
    ctx: Context, callback: (String) -> Unit
) {
    val br = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            callback(intent.data!!.schemeSpecificPart)
        }
    }
    val filter = IntentFilter()
    filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
    filter.addDataScheme("package")
    ctx.registerReceiver(br, filter)
}

fun parsePackageNames(input: String) = input.lines().filter { it.isNotEmpty() }

val getInstalledAppsFlags =
    if(VERSION.SDK_INT >= 24) PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_UNINSTALLED_PACKAGES else 0

fun searchInString(query: String, content: String)
        = query.split(' ').all { content.contains(it, true) }

class ToastChannel(val context: Context) {
    val channel = Channel<String>(0, BufferOverflow.DROP_LATEST)
    fun sendStatus(status: Boolean) {
        val resId = if (status) R.string.success else R.string.failed
        channel.trySend(context.getString(resId))
    }
    fun sendText(text: String) {
        channel.trySend(text)
    }
    fun sendText(resId: Int) {
        channel.trySend(context.getString(resId))
    }
}

class AppInfo(
    val name: String,
    val label: String,
    val icon: Drawable,
    val flags: Int
)

fun getAppInfo(pm: PackageManager, info: ApplicationInfo) =
    AppInfo(info.packageName, info.loadLabel(pm).toString(), info.loadIcon(pm), info.flags)

fun getAppInfo(pm: PackageManager, name: String): AppInfo {
    return try {
        getAppInfo(pm, pm.getApplicationInfo(name, getInstalledAppsFlags))
    } catch (_: PackageManager.NameNotFoundException) {
        AppInfo(name, "???", Color.Transparent.toArgb().toDrawable(), 0)
    }
}

fun <T>List<T>.plusOrMinus(state: Boolean, item: T) = if (state) plus(item) else minus(item)
fun <T>List<T>.plusOrMinus(state: Boolean, items: Collection<T>) =
    if (state) plus(items) else minus(items)

fun uninstallPackage(
    application: MyApplication, privilegeHelper: PrivilegeHelper,
    packageName: String, onComplete: (String?) -> Unit
) {
    val action = "com.bintianqi.owndroid.action.PACKAGE_UNINSTALLED"
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val statusExtra = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, 999)
            if (statusExtra == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                @SuppressWarnings("UnsafeIntentLaunch")
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                confirmIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(confirmIntent)
            } else {
                context.unregisterReceiver(this)
                if (statusExtra == PackageInstaller.STATUS_SUCCESS) {
                    onComplete(null)
                } else {
                    onComplete(parsePackageInstallerMessage(context, intent))
                }
            }
        }
    }
    ContextCompat.registerReceiver(
        application, receiver, IntentFilter(action), null,
        null, ContextCompat.RECEIVER_NOT_EXPORTED
    )
    val intent = Intent(action).setPackage(application.packageName)
    val pi = if(VERSION.SDK_INT >= 34) {
        PendingIntent.getBroadcast(
            application, 0, intent,
            PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_MUTABLE
        ).intentSender
    } else {
        PendingIntent.getBroadcast(application, 0, intent, PendingIntent.FLAG_MUTABLE).intentSender
    }
    application.getPackageInstaller(privilegeHelper.dhizuku).uninstall(packageName, pi)
}

fun viewModelFactory(build: () -> ViewModel) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return build() as T
        }
    }

val String.isValidPackageName
    get() = Regex("""^(?:[a-zA-Z]\w*\.)+[a-zA-Z]\w*$""").matches(this)
