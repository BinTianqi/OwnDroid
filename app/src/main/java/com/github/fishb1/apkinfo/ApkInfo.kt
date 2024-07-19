/*
 *  Copyright (C) 2022 fishbone
 *
 *  This code is licensed under MIT license (see LICENSE file for details)
 */

package com.github.fishb1.apkinfo

import com.android.apksig.internal.apk.AndroidBinXmlParser
import com.android.apksig.internal.apk.AndroidBinXmlParser.XmlParserException
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ApkInfo(
    val compileSdkVersion: Int = 0,
    val compileSdkVersionCodename: String = "",
    val installLocation: String = "",
    val packageName: String = "",
    val platformBuildVersionCode: Int = 0,
    val platformBuildVersionName: String = "",
    val versionCode: Int = 0,
    val versionName: String = "",
) {

    companion object {

        private val EMPTY = ApkInfo()

        private const val MANIFEST_FILE_NAME = "AndroidManifest.xml"

        fun fromInputStream(stream: InputStream): ApkInfo {
            ZipInputStream(stream).use { zip ->
                var entry: ZipEntry?
                do {
                    entry = zip.nextEntry
                    if (entry?.name == MANIFEST_FILE_NAME) {
                        val data = ByteBuffer.wrap(zip.readBytes())
                        return try {
                            val parser = AndroidBinXmlParser(data)
                            ManifestUtils.readApkInfo(parser)
                        } catch (e: XmlParserException) {
                            EMPTY
                        }
                    }
                } while (entry != null)
            }
            return EMPTY
        }
    }
}
