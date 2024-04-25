package com.bintianqi.owndroid.dpm

import android.os.IBinder
import android.system.Os
import com.bintianqi.owndroid.IUserService
import java.io.BufferedReader
import java.io.InputStreamReader

var service:IUserService? = null

class ShizukuService: IUserService.Stub() {
    override fun asBinder(): IBinder {
        TODO("Not yet implemented")
    }

    override fun destroy(){ }

    override fun execute(command: String?): String {
        var result = ""
        val process:Process
        try {
            process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()
            if(exitCode!=0){ result+="Error: $exitCode" }
        }catch(e:Exception){
            e.printStackTrace()
            return e.toString()
        }
        try {
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            var outputLine: String
            while(outputReader.readLine().also {outputLine = it}!=null) { result+="$outputLine\n" }
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            var errorLine: String
            while(errorReader.readLine().also {errorLine = it}!=null) { result+="$errorLine\n" }
        } catch(e: NullPointerException) {
            e.printStackTrace()
        }
        if(result==""){ return "No result" }
        return result
    }

    override fun getUid(): String {
        return Os.getuid().toString()
    }
}
