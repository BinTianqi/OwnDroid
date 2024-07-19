package com.bintianqi.owndroid

import android.Manifest
import android.os.Build.VERSION
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.dpm.selectedPermission
import com.bintianqi.owndroid.ui.NavIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionPicker(navCtrl: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.permission_picker)) },
                navigationIcon = { NavIcon{ navCtrl.navigateUp() } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())
        ) {
            items(permissionList()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable{
                            selectedPermission.value = it.permission
                            navCtrl.navigateUp()
                        }
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(it.icon),
                        contentDescription = stringResource(it.label),
                        modifier = Modifier.padding(start = 8.dp, end = 10.dp)
                    )
                    Column {
                        Text(text = stringResource(it.label))
                        Text(text = it.permission, modifier = Modifier.alpha(0.8F), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            items(1) { Spacer(Modifier.padding(vertical = 30.dp)) }
        }
    }
}

private data class PermissionPickerItem(
    val permission: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int
)

private fun permissionList(): List<PermissionPickerItem>{
    val list = mutableListOf<PermissionPickerItem>()
    list.add(PermissionPickerItem(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_READ_EXTERNAL_STORAGE, R.drawable.folder_fill0))
    list.add(PermissionPickerItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_WRITE_EXTERNAL_STORAGE, R.drawable.folder_fill0))
    if(VERSION.SDK_INT >= 33) {
        list.add(PermissionPickerItem(Manifest.permission.READ_MEDIA_AUDIO, R.string.permission_READ_MEDIA_AUDIO, R.drawable.music_note_fill0))
        list.add(PermissionPickerItem(Manifest.permission.READ_MEDIA_VIDEO, R.string.permission_READ_MEDIA_VIDEO, R.drawable.movie_fill0))
        list.add(PermissionPickerItem(Manifest.permission.READ_MEDIA_IMAGES, R.string.permission_READ_MEDIA_IMAGES, R.drawable.image_fill0))
    }
    list.add(PermissionPickerItem(Manifest.permission.CAMERA, R.string.permission_CAMERA, R.drawable.photo_camera_fill0))
    list.add(PermissionPickerItem(Manifest.permission.RECORD_AUDIO, R.string.permission_RECORD_AUDIO, R.drawable.mic_fill0))
    list.add(PermissionPickerItem(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.permission_ACCESS_COARSE_LOCATION, R.drawable.location_on_fill0))
    list.add(PermissionPickerItem(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_ACCESS_FINE_LOCATION, R.drawable.location_on_fill0))
    if(VERSION.SDK_INT >= 29) {
        list.add(PermissionPickerItem(Manifest.permission.ACCESS_BACKGROUND_LOCATION, R.string.permission_ACCESS_BACKGROUND_LOCATION, R.drawable.location_on_fill0))
    }
    list.add(PermissionPickerItem(Manifest.permission.READ_CONTACTS, R.string.permission_READ_CONTACTS, R.drawable.contacts_fill0))
    list.add(PermissionPickerItem(Manifest.permission.WRITE_CONTACTS, R.string.permission_WRITE_CONTACTS, R.drawable.contacts_fill0))
    list.add(PermissionPickerItem(Manifest.permission.READ_CALENDAR, R.string.permission_READ_CALENDAR, R.drawable.calendar_month_fill0))
    list.add(PermissionPickerItem(Manifest.permission.WRITE_CALENDAR, R.string.permission_WRITE_CALENDAR, R.drawable.calendar_month_fill0))
    list.add(PermissionPickerItem(Manifest.permission.CALL_PHONE, R.string.permission_CALL_PHONE, R.drawable.call_fill0))
    list.add(PermissionPickerItem(Manifest.permission.READ_PHONE_STATE, R.string.permission_READ_PHONE_STATE, R.drawable.mobile_phone_fill0))
    list.add(PermissionPickerItem(Manifest.permission.READ_SMS, R.string.permission_READ_SMS, R.drawable.sms_fill0))
    list.add(PermissionPickerItem(Manifest.permission.RECEIVE_SMS, R.string.permission_RECEIVE_SMS, R.drawable.sms_fill0))
    list.add(PermissionPickerItem(Manifest.permission.SEND_SMS, R.string.permission_SEND_SMS, R.drawable.sms_fill0))
    list.add(PermissionPickerItem(Manifest.permission.READ_CALL_LOG, R.string.permission_READ_CALL_LOG, R.drawable.call_log_fill0))
    list.add(PermissionPickerItem(Manifest.permission.WRITE_CALL_LOG, R.string.permission_WRITE_CALL_LOG, R.drawable.call_log_fill0))
    list.add(PermissionPickerItem(Manifest.permission.BODY_SENSORS, R.string.permission_BODY_SENSORS, R.drawable.sensors_fill0))
    if(VERSION.SDK_INT >= 33) {
        list.add(PermissionPickerItem(Manifest.permission.BODY_SENSORS_BACKGROUND, R.string.permission_BODY_SENSORS_BACKGROUND, R.drawable.sensors_fill0))
    }
    if(VERSION.SDK_INT > 29) {
        list.add(PermissionPickerItem(Manifest.permission.ACTIVITY_RECOGNITION, R.string.permission_ACTIVITY_RECOGNITION, R.drawable.history_fill0))
    }
    if(VERSION.SDK_INT >= 33) {
        list.add(PermissionPickerItem(Manifest.permission.POST_NOTIFICATIONS, R.string.permission_POST_NOTIFICATIONS, R.drawable.notifications_fill0))
    }
    return list
}
