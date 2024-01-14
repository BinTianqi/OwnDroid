package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.UserManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
fun UserRestriction(myDpm: DevicePolicyManager, myComponent: ComponentName){
    val verticalScrolling = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(verticalScrolling)) {
        UserRestrictionItem(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,R.string.config_mobile_network,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_CONFIG_WIFI,R.string.config_wifi,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_BLUETOOTH,R.string.bluetooth,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_AIRPLANE_MODE,R.string.airplane_mode,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_CONFIG_LOCATION,R.string.config_location,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_DEBUGGING_FEATURES,R.string.debug_features,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_CREATE_WINDOWS,R.string.create_windows,myComponent, myDpm)
    }
}

@Composable
private fun UserRestrictionItem(restriction:String, itemName:Int, myComponent: ComponentName, myDpm: DevicePolicyManager){
    val strictState = myDpm.getUserRestrictions(myComponent)
    val currentState = stringResource(R.string.is_disallow)+strictState.getBoolean("no_create_windows").toString()
    Column{
        Text(
            text = stringResource(itemName),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(text = currentState)
        Button(onClick = {myDpm.clearUserRestriction(myComponent,restriction)}) {
            Text(stringResource(R.string.disallow))
        }
        Button(onClick = {myDpm.addUserRestriction(myComponent,restriction)}) {
            Text(text = stringResource(R.string.allow))
        }
    }
}
