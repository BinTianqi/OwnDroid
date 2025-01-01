package com.bintianqi.owndroid.dpm

import android.system.Os
import androidx.annotation.Keep
import com.bintianqi.owndroid.IUserService
import java.io.BufferedReader
import java.io.InputStreamReader

@Keep
class ShizukuService: IUserService.Stub() {
    override fun execute(command: String): String {
        var result = ""
        val process: Process
        try {
            process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()
            if(exitCode != 0) { result += "Error: $exitCode" }
        } catch(e: Exception) {
            e.printStackTrace()
            return e.toString()
        }
        try {
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            var outputLine: String
            while(outputReader.readLine().also {outputLine = it} != null) { result += "$outputLine\n" }
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            var errorLine: String
            while(errorReader.readLine().also {errorLine = it} != null) { result += "$errorLine\n" }
        } catch(e: NullPointerException) {
            e.printStackTrace()
        }
        return result
    }

    override fun getUid(): Int = Os.getuid()
}
