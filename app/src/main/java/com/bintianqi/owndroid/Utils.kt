package com.bintianqi.owndroid

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
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
import java.util.concurrent.TimeUnit
import kotlin.io.encoding.Base64

var zhCN = true

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
    @StringRes get() = if(this) R.string.yes else R.string.no

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

fun exportLogs(context: Context, uri: Uri) {
    context.contentResolver.openOutputStream(uri)?.use { output ->
        val proc = Runtime.getRuntime().exec("logcat -d")
        proc.inputStream.copyTo(output)
        if(Build.VERSION.SDK_INT >= 26) proc.waitFor(2L, TimeUnit.SECONDS)
        else proc.waitFor()
        context.showOperationResultToast(proc.exitValue() == 0)
    }
}

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
    val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
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