package com.bintianqi.owndroid

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.Keep
import rikka.shizuku.Shizuku
import rikka.sui.Sui
import kotlin.system.exitProcess

@Keep
class ShizukuService: IUserService.Stub() {
    override fun execute(command: String): Bundle? {
        try {
            val bundle = Bundle()
            val process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()
            bundle.putInt("code", exitCode)
            bundle.putString("output",  process.inputStream.readBytes().decodeToString())
            bundle.putString("error", process.errorStream.readBytes().decodeToString())
            return bundle
        } catch(e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun destroy() {
        exitProcess(0)
    }
}

fun getShizukuArgs(context: Context): Shizuku.UserServiceArgs {
    return Shizuku.UserServiceArgs(ComponentName(context, ShizukuService::class.java))
        .daemon(false)
        .processNameSuffix("shizuku-service")
        .debuggable(false)
        .version(1)
}

fun useShizuku(context: Context, action: (IBinder?) -> Unit) {
    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            action(service)
            Shizuku.unbindUserService(getShizukuArgs(context), this, true)
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    try {
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            Shizuku.bindUserService(getShizukuArgs(context), connection)
        } else if(Shizuku.shouldShowRequestPermissionRationale()) {
            context.popToast(R.string.permission_denied)
        } else {
            Sui.init(context.packageName)
            fun requestPermissionResultListener(requestCode: Int, grantResult: Int) {
                if(grantResult != PackageManager.PERMISSION_GRANTED) {
                    context.popToast(R.string.permission_denied)
                }
                Shizuku.removeRequestPermissionResultListener(::requestPermissionResultListener)
            }
            Shizuku.addRequestPermissionResultListener(::requestPermissionResultListener)
            Shizuku.requestPermission(0)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}