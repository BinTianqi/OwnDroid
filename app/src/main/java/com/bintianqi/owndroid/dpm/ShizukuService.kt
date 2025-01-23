package com.bintianqi.owndroid.dpm

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import android.system.Os
import androidx.annotation.Keep
import com.bintianqi.owndroid.IUserService
import com.bintianqi.owndroid.getContext
import kotlin.system.exitProcess

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
            val outputReader = process.inputStream.bufferedReader()
            var outputLine: String
            while(outputReader.readLine().also {outputLine = it} != null) { result += "$outputLine\n" }
            val errorReader = process.errorStream.bufferedReader()
            var errorLine: String
            while(errorReader.readLine().also {errorLine = it} != null) { result += "$errorLine\n" }
        } catch(e: NullPointerException) {
            e.printStackTrace()
        }
        return result
    }

    override fun getUid(): Int = Os.getuid()

    @SuppressLint("MissingPermission")
    override fun listAccounts(): Array<Account> {
        val am = getContext().getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        return am.accounts
    }

    override fun destroy() {
        exitProcess(0)
    }
}
