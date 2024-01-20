package com.binbin.androidowner

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast

class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "已启用", Toast.LENGTH_SHORT).show()
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Toast.makeText(context, "已接收", Toast.LENGTH_SHORT).show()
    }
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        Toast.makeText(context, "撤销授权", Toast.LENGTH_SHORT).show()
        return "这是取消时的提示"
    }
    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "已禁用", Toast.LENGTH_SHORT).show()
    }
    override fun onSystemUpdatePending(context: Context, intent: Intent, receivedTime: Long) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        Toast.makeText(context, "新的系统更新！", Toast.LENGTH_SHORT).show()
    }
}