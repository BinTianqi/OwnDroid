package com.bintianqi.owndroid

import android.app.Activity
import android.os.Bundle
import com.bintianqi.owndroid.dpm.getDPM

class ShortcutsReceiverActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if(intent.action == "com.bintianqi.owndroid.action.LOCK") {
                getDPM().lockNow()
            }
        } catch(_: Exception) {}
        finish()
    }
}