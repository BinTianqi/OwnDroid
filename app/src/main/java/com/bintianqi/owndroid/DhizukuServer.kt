package com.bintianqi.owndroid

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.rosan.dhizuku.aidl.IDhizukuClient
import com.rosan.dhizuku.aidl.IDhizukuRequestPermissionListener
import com.rosan.dhizuku.server_api.DhizukuProvider
import com.rosan.dhizuku.server_api.DhizukuService
import com.rosan.dhizuku.shared.DhizukuVariables
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val TAG = "DhizukuServer"

const val DHIZUKU_CLIENTS_FILE = "dhizuku_clients.json"

class MyDhizukuProvider(): DhizukuProvider() {
    override fun onCreateService(client: IDhizukuClient): DhizukuService? {
        Log.d(TAG, "Creating MyDhizukuService")
        return if (SP.dhizukuServer) MyDhizukuService(context!!, MyAdminComponent, client) else null
    }
}

class MyDhizukuService(context: Context, admin: ComponentName, client: IDhizukuClient) :
    DhizukuService(context, admin, client) {
    override fun checkCallingPermission(func: String?, callingUid: Int, callingPid: Int): Boolean {
        if (!SP.dhizukuServer) return false
        val pm = mContext.packageManager
        val packageInfo = pm.getPackageInfo(
            pm.getNameForUid(callingUid) ?: return false,
            if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        )
        val file = mContext.filesDir.resolve(DHIZUKU_CLIENTS_FILE)
        val clients = Json.decodeFromString<List<DhizukuClientInfo>>(file.readText())
        val signature = getPackageSignature(packageInfo)
        val requiredPermission = when (func) {
            "remote_transact", "remote_process" -> func
            "bind_user_service", "unbind_user_service" -> "user_service"
            "get_delegated_scopes", "set_delegated_scopes" -> "delegated_scopes"
            else -> "other"
        }
        val hasPermission = clients.find {
            callingUid == it.uid && signature == it.signature && requiredPermission in it.permissions
        } != null
        Log.d(TAG, "UID $callingUid, PID $callingPid, required permission: $requiredPermission, has permission: $hasPermission")
        return hasPermission
    }

    override fun getVersionName() = "1.0"
}

class DhizukuActivity : ComponentActivity() {
    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SP.dhizukuServer) {
            finish()
            return
        }
        val bundle = intent.extras ?: return
        val uid = bundle.getInt(DhizukuVariables.PARAM_CLIENT_UID, -1)
        if (uid == -1) return
        val binder = bundle.getBinder(DhizukuVariables.PARAM_CLIENT_REQUEST_PERMISSION_BINDER) ?: return
        val listener = IDhizukuRequestPermissionListener.Stub.asInterface(binder)
        val packageName = packageManager.getPackagesForUid(uid)?.first() ?: return
        val packageInfo = packageManager.getPackageInfo(
            packageName,
            if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        )
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        val icon = appInfo.loadIcon(packageManager)
        val label = appInfo.loadLabel(packageManager).toString()
        fun close(grantPermission: Boolean) {
            val file = filesDir.resolve(DHIZUKU_CLIENTS_FILE)
            val json = Json { ignoreUnknownKeys = true }
            val clients = json.decodeFromString<MutableList<DhizukuClientInfo>>(file.readText())
            val index = clients.indexOfFirst { it.uid == uid }
            val clientInfo = DhizukuClientInfo(
                uid, getPackageSignature(packageInfo), if (grantPermission) DhizukuPermissions else emptyList()
            )
            if (index == -1) clients += clientInfo
            else clients[index] = clientInfo
            file.writeText(Json.encodeToString(clients))
            finish()
            listener.onRequestPermission(
                if (grantPermission) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            )
        }
        val vm by viewModels<MyViewModel>()
        enableEdgeToEdge()
        setContent {
            var appLockDialog by remember { mutableStateOf(false) }
            val theme by vm.theme.collectAsStateWithLifecycle()
            OwnDroidTheme(theme) {
                if (!appLockDialog) AlertDialog(
                    icon = {
                        Image(rememberDrawablePainter(icon), null, Modifier.size(35.dp))
                    },
                    title = {
                        Text(stringResource(R.string.request_permission))
                    },
                    text = {
                        Text("$label\n($packageName)")
                    },
                    confirmButton = {
                        var time by remember { mutableIntStateOf(3) }
                        LaunchedEffect(Unit) {
                            for (i in 2 downTo 0) {
                                delay(1000)
                                time = i
                            }
                        }
                        TextButton({
                            if (SP.lockPasswordHash.isNullOrEmpty()) {
                                close(true)
                            } else {
                                appLockDialog = true
                            }
                        }, enabled = time == 0) {
                            val append = if (time > 0) " (${time}s)" else ""
                            Text(stringResource(R.string.allow) + append)
                        }
                    },
                    dismissButton = {
                        TextButton({
                            close(false)
                        }) {
                            Text(stringResource(R.string.reject))
                        }
                    },
                    onDismissRequest = { close(false) }
                )
                else AppLockDialog({ close(true) }) { close(false) }
            }
        }
    }
}

val DhizukuPermissions = listOf("remote_transact", "remote_process", "user_service", "delegated_scopes", "other")

@Serializable
data class DhizukuClientInfo(
    val uid: Int,
    val signature: String?,
    val permissions: List<String> = emptyList()
)