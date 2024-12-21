package com.bintianqi.owndroid

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.bintianqi.owndroid.dpm.addDeviceAdmin
import com.bintianqi.owndroid.dpm.createManagedProfile
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

lateinit var getFile: ActivityResultLauncher<Intent>
val fileUriFlow = MutableStateFlow(Uri.parse(""))

var zhCN = true

fun uriToStream(
    context: Context,
    uri: Uri?,
    operation: (stream: InputStream)->Unit
){
    if(uri!=null){
        try {
            val stream = context.contentResolver.openInputStream(uri)
            if(stream != null) { operation(stream) }
            stream?.close()
        }
        catch(_: FileNotFoundException) { Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_SHORT).show() }
        catch(_: IOException) { Toast.makeText(context, R.string.io_exception, Toast.LENGTH_SHORT).show() }
    }
}

fun MutableList<Int>.toggle(status: Boolean, element: Int) {
    if(status) add(element) else remove(element)
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

lateinit var requestPermission: ActivityResultLauncher<String>
lateinit var exportFile: ActivityResultLauncher<Intent>
var exportFilePath: String? = null
var isExportingSecurityOrNetworkLogs = false

fun registerActivityResult(context: ComponentActivity){
    getFile = context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        activityResult.data.let {
            if(it != null) fileUriFlow.value = it.data
        }
    }
    createManagedProfile = context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    addDeviceAdmin = context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val dpm = context.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if(dpm.isAdminActive(ComponentName(context.applicationContext, Receiver::class.java))) {
            backToHomeStateFlow.value = true
        }
    }
    requestPermission = context.registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted.value = it }
    exportFile = context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intentData = result.data ?: return@registerForActivityResult
        val uriData = intentData.data ?: return@registerForActivityResult
        val path = exportFilePath ?: return@registerForActivityResult
        context.contentResolver.openOutputStream(uriData).use { outStream ->
            if(outStream != null) {
                if(isExportingSecurityOrNetworkLogs) outStream.write("[".encodeToByteArray())
                File(path).inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
                if(isExportingSecurityOrNetworkLogs) outStream.write("]".encodeToByteArray())
                Toast.makeText(context.applicationContext, R.string.success, Toast.LENGTH_SHORT).show()
            }
        }
        isExportingSecurityOrNetworkLogs = false
        exportFilePath = null
    }
}

val permissionGranted = MutableStateFlow<Boolean?>(null)

suspend fun prepareForNotification(context: Context, action: ()->Unit) {
    if(VERSION.SDK_INT >= 33) {
        if(context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            action()
        } else {
            requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            permissionGranted.collect { if(it == true) action() }
        }
    } else {
        action()
    }
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

val Long.humanReadableDate: String
    get() = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(this))

val Long.humanReadableTime: String
    get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(this))

