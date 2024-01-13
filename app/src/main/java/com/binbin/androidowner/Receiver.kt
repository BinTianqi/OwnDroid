package com.binbin.androidowner

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    val TAG = "MyDeviceAdminReceiver"
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.e(">>>>>>>>>", "onEnabled")
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.e(">>>>>>>>>", "onReceive")
    }
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        val strResult = "这是取消时的提示"
        Log.e(">>>>>>>>>", "onDisableRequested")
        return strResult
    }
    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.e(">>>>>>>>>", "onDisabled")
    }
}