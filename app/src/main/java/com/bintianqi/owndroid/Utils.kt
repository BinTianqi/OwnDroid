package com.bintianqi.owndroid

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.reflect.typeOf

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
    catch(_: FileNotFoundException) { Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_SHORT).show() }
    catch(_: IOException) { Toast.makeText(context, R.string.io_exception, Toast.LENGTH_SHORT).show() }
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

@RequiresApi(26)
fun parseTimestamp(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

fun parseDate(date: Date): String = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(date)

val Long.humanReadableDate: String
    get() = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(this))

fun formatDate(pattern: String, value: Long): String
    = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(value))

fun Context.showOperationResultToast(success: Boolean) {
    Toast.makeText(this, if(success) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
}

@SuppressLint("PrivateApi")
fun getContext(): Context {
    return Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null) as Context
}

const val APK_MIME = "application/vnd.android.package-archive"

inline fun <reified T> serializableNavTypePair() =
    typeOf<T>() to object : NavType<T>(false) {
    override fun get(bundle: Bundle, key: String): T? =
        bundle.getString(key)?.let { parseValue(it) }
    override fun put(bundle: Bundle, key: String, value: T) =
        bundle.putString(key, serializeAsValue(value))
    override fun parseValue(value: String): T =
        Json.decodeFromString(value)
    override fun serializeAsValue(value: T): String =
        Json.encodeToString(value)
}

class ChoosePackageContract: ActivityResultContract<Nothing?, String?>() {
    override fun createIntent(context: Context, input: Nothing?): Intent =
        Intent(context, PackageChooserActivity::class.java)
    override fun parseResult(resultCode: Int, intent: Intent?): String? =
        intent?.getStringExtra("package")
}

fun exportLogs(context: Context, uri: Uri) {
    context.contentResolver.openOutputStream(uri)?.use { output ->
        val proc = Runtime.getRuntime().exec("logcat -d")
        proc.inputStream.copyTo(output)
        if(Build.VERSION.SDK_INT >= 26) proc.waitFor(2L, TimeUnit.SECONDS)
        else proc.waitFor()
        context.showOperationResultToast(proc.exitValue() == 0)
    }
}

fun <T> NavHostController.navigate(route: T, args: Bundle) {
    navigate(graph.findNode(route)!!.id, args)
}

val HorizontalPadding = 16.dp
