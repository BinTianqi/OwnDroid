package com.binbin.androidowner;

import android.content.pm.PackageManager;
import android.util.Log;
import rikka.shizuku.Shizuku;

public class ShizukuUtil {
    private static void onRequestPermissionsResult(int requestCode, int grantResult) {
        boolean granted = PackageManager.PERMISSION_GRANTED == grantResult;
        Log.d("ShizukuUtil","RequestCode: "+requestCode);
        Log.d("ShizukuUtil","GrantState: "+granted);
    }
    static final Shizuku.OnRequestPermissionResultListener requestPermissionListener = ShizukuUtil::onRequestPermissionsResult;
    static final Shizuku.OnBinderReceivedListener binderReceivedListener = () -> Log.d("ShizukuUtil","Binder received");
    static final Shizuku.OnBinderDeadListener binderDeadListener = () -> Log.e("ShizukuUtil","Binder dead");
}
