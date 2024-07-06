/*
 *  Copyright (C) 2022 fishbone
 *
 *  This code is licensed under MIT license (see LICENSE file for details)
 */

package com.github.fishb1.apkinfo

import com.android.apksig.internal.apk.AndroidBinXmlParser

internal object ManifestUtils {

    private const val TAG_MANIFEST = "manifest"

    private const val ATTR_COMPILE_SDK_VERSION = "compileSdkVersion"
    private const val ATTR_COMPILE_SDK_VERSION_CODENAME = "compileSdkVersionCodename"
    private const val ATTR_INSTALL_LOCATION = "installLocation"
    private const val ATTR_PACKAGE = "package"
    private const val ATTR_PLATFORM_BUILD_VERSION_CODE = "platformBuildVersionCode"
    private const val ATTR_PLATFORM_BUILD_VERSION_NAME = "platformBuildVersionName"
    private const val ATTR_VERSION_CODE = "versionCode"
    private const val ATTR_VERSION_NAME = "versionName"

    fun readApkInfo(parser: AndroidBinXmlParser): ApkInfo {
        val builder = ApkInfoBuilder()
        var eventType = parser.eventType
        while (eventType != AndroidBinXmlParser.EVENT_END_DOCUMENT) {
            if (eventType == AndroidBinXmlParser.EVENT_START_ELEMENT && parser.name == TAG_MANIFEST) {

                for (i in 0 until parser.attributeCount) {
                    when (parser.getAttributeName(i)) {
                        ATTR_COMPILE_SDK_VERSION ->
                            builder.compileSdkVersion(parser.getAttributeIntValue(i))
                        ATTR_COMPILE_SDK_VERSION_CODENAME ->
                            builder.compileSdkVersionCodename(parser.getAttributeStringValue(i))
                        ATTR_INSTALL_LOCATION ->
                            builder.installLocation(parser.getAttributeStringValue(i))
                        ATTR_PACKAGE ->
                            builder.packageName(parser.getAttributeStringValue(i))
                        ATTR_PLATFORM_BUILD_VERSION_CODE ->
                            builder.platformBuildVersionCode(parser.getAttributeIntValue(i))
                        ATTR_PLATFORM_BUILD_VERSION_NAME ->
                            builder.platformBuildVersionName(parser.getAttributeStringValue(i))
                        ATTR_VERSION_CODE ->
                            builder.versionCode(parser.getAttributeIntValue(i))
                        ATTR_VERSION_NAME ->
                            builder.versionName(parser.getAttributeStringValue(i))
                    }
                }
            }
            eventType = parser.next()
        }
        return builder.build()
    }
}
