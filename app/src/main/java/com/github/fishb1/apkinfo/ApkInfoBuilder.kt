/*
 *  Copyright (C) 2022 fishbone
 *
 *  This code is licensed under MIT license (see LICENSE file for details)
 */

package com.github.fishb1.apkinfo

internal class ApkInfoBuilder {

    private var compileSdkVersion: Int = 0
    private var compileSdkVersionCodename: String = ""
    private var installLocation: String = ""
    private var packageName: String = ""
    private var platformBuildVersionCode: Int = 0
    private var platformBuildVersionName: String = ""
    private var versionCode: Int = 0
    private var versionName: String = ""

    fun compileSdkVersion(value: Int) = apply {
        compileSdkVersion = value
    }

    fun compileSdkVersionCodename(value: String) = apply {
        compileSdkVersionCodename = value
    }

    fun installLocation(value: String) = apply {
        installLocation = value
    }

    fun packageName(value: String) = apply {
        packageName = value
    }

    fun platformBuildVersionCode(value: Int) = apply {
        platformBuildVersionCode = value
    }

    fun platformBuildVersionName(value: String) = apply {
        platformBuildVersionName = value
    }

    fun versionCode(value: Int) = apply {
        versionCode = value
    }

    fun versionName(value: String) = apply {
        versionName = value
    }

    fun build(): ApkInfo {
        return ApkInfo(
            compileSdkVersion = compileSdkVersion,
            compileSdkVersionCodename =compileSdkVersionCodename,
            installLocation = installLocation,
            packageName = packageName,
            platformBuildVersionCode = platformBuildVersionCode,
            platformBuildVersionName = platformBuildVersionName,
            versionCode = versionCode,
            versionName = versionName,
        )
    }
}
