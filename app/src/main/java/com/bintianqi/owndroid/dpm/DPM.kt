package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.IDevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build.VERSION
import androidx.activity.ComponentActivity.CONTEXT_IGNORE_SECURITY
import androidx.activity.ComponentActivity.DEVICE_POLICY_SERVICE
import androidx.activity.result.ActivityResultLauncher
import com.bintianqi.owndroid.PackageInstallerReceiver
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.Dhizuku.binderWrapper
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException
import java.io.InputStream

var selectedPermission = MutableStateFlow("")
lateinit var createManagedProfile: ActivityResultLauncher<Intent>
lateinit var addDeviceAdmin: ActivityResultLauncher<Intent>

fun isDeviceOwner(dpm: DevicePolicyManager): Boolean {
    return dpm.isDeviceOwnerApp("com.bintianqi.owndroid")
}

fun isProfileOwner(dpm: DevicePolicyManager): Boolean {
    return dpm.isProfileOwnerApp("com.bintianqi.owndroid")
}

fun DevicePolicyManager.isOrgProfile(receiver: ComponentName):Boolean {
    return VERSION.SDK_INT >= 30 && isProfileOwner(this) && isManagedProfile(receiver) && isOrganizationOwnedDeviceWithManagedProfile
}

@Throws(IOException::class)
fun installPackage(context: Context, inputStream: InputStream) {
    val packageInstaller = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
    val sessionId = packageInstaller.createSession(params)
    val session = packageInstaller.openSession(sessionId)
    val out = session.openWrite("COSU", 0, -1)
    val buffer = ByteArray(65536)
    var c: Int
    while(inputStream.read(buffer).also{c = it}!=-1) { out.write(buffer, 0, c) }
    session.fsync(out)
    inputStream.close()
    out.close()
    val intent = Intent(context, PackageInstallerReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, PendingIntent.FLAG_IMMUTABLE).intentSender
    session.commit(pendingIntent)
}

@SuppressLint("PrivateApi")
fun binderWrapperDevicePolicyManager(appContext: Context): DevicePolicyManager {
    try {
        val context = appContext.createPackageContext(Dhizuku.getOwnerComponent().packageName, CONTEXT_IGNORE_SECURITY)
        val manager = context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val field = manager.javaClass.getDeclaredField("mService")
        field.isAccessible = true
        val oldInterface = field[manager] as IDevicePolicyManager
        if (oldInterface is DhizukuBinderWrapper) return manager
        val oldBinder = oldInterface.asBinder()
        val newBinder = binderWrapper(oldBinder)
        val newInterface = IDevicePolicyManager.Stub.asInterface(newBinder)
        field[manager] = newInterface
        return manager
    } catch (e: NoSuchFieldException) {
        throw RuntimeException(e)
    } catch (e: IllegalAccessException) {
        throw RuntimeException(e)
    } catch (e: PackageManager.NameNotFoundException) {
        throw RuntimeException(e)
    }
}
