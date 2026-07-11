package com.bintianqi.owndroid.feature.applications

import com.bintianqi.owndroid.utils.AppInfo
import kotlinx.serialization.Serializable

class AppChooserEntry(
    val info: AppInfo,
    val hasMc: Boolean, // Managed configuration
    val mcModified: Boolean,
    val suspended: Boolean,
    val hidden: Boolean,
    val ub: Boolean, // Uninstall blocked
    val ucd: Boolean, // User control disabled
    val mdd: Boolean, // Metered data disabled
)

@Serializable
data class AppChooserFilter(
    val userApps: Boolean = true,
    val systemApps: Boolean = false,
    val hasMc: Boolean = false,
    val mcModified: Boolean = false,
    val suspended: Boolean = true,
    val notSuspended: Boolean = true,
    val hidden: Boolean = true,
    val notHidden: Boolean = true,
    val ub: Boolean = true,
    val notUb: Boolean = true,
    val ucDisabled: Boolean = true,
    val ucNotDisabled: Boolean = true,
    val mdDisabled: Boolean = true,
    val mdNotDisabled: Boolean = true,
    val installed: Boolean = true
)
