package com.bintianqi.owndroid.feature.network

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.utils.PrivilegeStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NetworkStatsViewModel(
    val application: MyApplication, val privilegeState: StateFlow<PrivilegeStatus>
) : ViewModel() {
    var statsData = emptyList<NetworkStatsData>()
    fun readNetworkStats(stats: NetworkStats): List<NetworkStatsData> {
        val list = mutableListOf<NetworkStatsData>()
        while (stats.hasNextBucket()) {
            val bucket = NetworkStats.Bucket()
            stats.getNextBucket(bucket)
            list += readDataFromBucket(bucket)
        }
        stats.close()
        return list
    }

    @RequiresApi(24)
    fun getPackageUid(name: String): Int {
        return application.packageManager.getPackageUid(name, 0)
    }

    fun readDataFromBucket(bucket: NetworkStats.Bucket): NetworkStatsData {
        return NetworkStatsData(
            bucket.rxBytes, bucket.rxPackets, bucket.txBytes, bucket.txPackets,
            bucket.uid, bucket.state, bucket.startTimeStamp, bucket.endTimeStamp,
            if (VERSION.SDK_INT >= 24) bucket.tag else null,
            if (VERSION.SDK_INT >= 24) bucket.roaming else null,
            if (VERSION.SDK_INT >= 26) bucket.metered else null
        )
    }

    @Suppress("NewApi")
    fun queryStats(params: QueryNetworkStatsParams, callback: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val nsm = application.getSystemService(NetworkStatsManager::class.java)
            try {
                val data = when (params.target) {
                    NetworkStatsTarget.Device -> listOf(
                        readDataFromBucket(
                            nsm.querySummaryForDevice(
                                params.networkType.type, null, params.startTime, params.endTime
                            )
                        )
                    )

                    NetworkStatsTarget.User -> listOf(
                        readDataFromBucket(
                            nsm.querySummaryForUser(
                                params.networkType.type, null, params.startTime, params.endTime
                            )
                        )
                    )

                    NetworkStatsTarget.Uid -> readNetworkStats(
                        nsm.queryDetailsForUid(
                            params.networkType.type, null, params.startTime, params.endTime,
                            params.uid
                        )
                    )

                    NetworkStatsTarget.UidTag -> readNetworkStats(
                        nsm.queryDetailsForUidTag(
                            params.networkType.type, null, params.startTime, params.endTime,
                            params.uid, params.tag
                        )
                    )

                    NetworkStatsTarget.UidTagState -> readNetworkStats(
                        nsm.queryDetailsForUidTagState(
                            params.networkType.type, null, params.startTime, params.endTime,
                            params.uid, params.tag, params.state.id
                        )
                    )
                }
                statsData = data
                withContext(Dispatchers.Main) {
                    if (data.isEmpty()) {
                        callback(application.getString(R.string.no_data))
                    } else {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(e.message ?: "")
                }
            }
        }
    }
}
