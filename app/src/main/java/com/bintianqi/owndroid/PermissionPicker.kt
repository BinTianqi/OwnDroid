package com.bintianqi.owndroid

import android.Manifest
import android.os.Build.VERSION
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.dpm.applySelectedPermission
import com.bintianqi.owndroid.dpm.selectedPermission
import com.bintianqi.owndroid.ui.NavIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionPicker(navCtrl: NavHostController){
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = stringResource(R.string.permission_picker))},
                navigationIcon = {NavIcon{navCtrl.navigateUp()}},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ){ paddingValues->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())
        ){
            items(permissionList()){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable{
                            selectedPermission = it.first
                            applySelectedPermission.value = true
                            navCtrl.navigateUp()
                        }
                        .padding(vertical = 6.dp, horizontal = 8.dp)
                ){
                    Text(text = it.first)
                    Text(text = stringResource(it.second), modifier = Modifier.alpha(0.8F))
                }
            }
            items(1){ Spacer(Modifier.padding(vertical = 30.dp)) }
        }
    }
}

private fun permissionList():List<Pair<String,Int>>{
    val list = mutableListOf<Pair<String,Int>>()
    list.add(Pair(Manifest.permission.READ_EXTERNAL_STORAGE,R.string.permission_READ_EXTERNAL_STORAGE))
    list.add(Pair(Manifest.permission.WRITE_EXTERNAL_STORAGE,R.string.permission_WRITE_EXTERNAL_STORAGE))
    if(VERSION.SDK_INT>=33){
        list.add(Pair(Manifest.permission.READ_MEDIA_AUDIO,R.string.permission_READ_MEDIA_AUDIO))
        list.add(Pair(Manifest.permission.READ_MEDIA_VIDEO,R.string.permission_READ_MEDIA_VIDEO))
        list.add(Pair(Manifest.permission.READ_MEDIA_IMAGES,R.string.permission_READ_MEDIA_IMAGES))
    }
    list.add(Pair(Manifest.permission.CAMERA,R.string.permission_CAMERA))
    list.add(Pair(Manifest.permission.RECORD_AUDIO,R.string.permission_RECORD_AUDIO))
    list.add(Pair(Manifest.permission.ACCESS_COARSE_LOCATION,R.string.permission_ACCESS_COARSE_LOCATION))
    list.add(Pair(Manifest.permission.ACCESS_FINE_LOCATION,R.string.permission_ACCESS_FINE_LOCATION))
    if(VERSION.SDK_INT>=29){
        list.add(Pair(Manifest.permission.ACCESS_BACKGROUND_LOCATION,R.string.permission_ACCESS_BACKGROUND_LOCATION))
    }
    list.add(Pair(Manifest.permission.READ_CONTACTS,R.string.permission_READ_CONTACTS))
    list.add(Pair(Manifest.permission.WRITE_CONTACTS,R.string.permission_WRITE_CONTACTS))
    list.add(Pair(Manifest.permission.READ_CALENDAR,R.string.permission_READ_CALENDAR))
    list.add(Pair(Manifest.permission.WRITE_CALENDAR,R.string.permission_WRITE_CALENDAR))
    list.add(Pair(Manifest.permission.CALL_PHONE,R.string.permission_CALL_PHONE))
    list.add(Pair(Manifest.permission.READ_PHONE_STATE,R.string.permission_READ_PHONE_STATE))
    list.add(Pair(Manifest.permission.READ_SMS,R.string.permission_READ_SMS))
    list.add(Pair(Manifest.permission.RECEIVE_SMS,R.string.permission_RECEIVE_SMS))
    list.add(Pair(Manifest.permission.SEND_SMS,R.string.permission_SEND_SMS))
    list.add(Pair(Manifest.permission.READ_CALL_LOG,R.string.permission_READ_CALL_LOG))
    list.add(Pair(Manifest.permission.WRITE_CALL_LOG,R.string.permission_WRITE_CALL_LOG))
    list.add(Pair(Manifest.permission.BODY_SENSORS,R.string.permission_BODY_SENSORS))
    if(VERSION.SDK_INT>=33){
        list.add(Pair(Manifest.permission.BODY_SENSORS_BACKGROUND,R.string.permission_BODY_SENSORS_BACKGROUND))
    }
    if(VERSION.SDK_INT>29){
        list.add(Pair(Manifest.permission.ACTIVITY_RECOGNITION,R.string.permission_ACTIVITY_RECOGNITION))
    }
    if(VERSION.SDK_INT>=33){
        list.add(Pair(Manifest.permission.POST_NOTIFICATIONS,R.string.permission_POST_NOTIFICATIONS))
    }
    //list.add(Pair(Manifest.permission.,R.string.))
    return list
}
