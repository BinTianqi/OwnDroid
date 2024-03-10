package com.binbin.androidowner.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.binbin.androidowner.R

@Composable
fun SubPageItem(
    @StringRes title: Int,
    desc:String,
    operation: () -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = operation).padding(vertical = 15.dp)
    ){
        Spacer(Modifier.fillMaxWidth(0.07F))
        Column {
            Text(text = stringResource(title), style = typography.titleLarge)
            if(desc!=""){Text(text = desc, color = colorScheme.onBackground.copy(alpha = 0.8F))}
        }
    }
}

@Composable
fun NavIcon(operation: () -> Unit){
    Icon(
        painter = painterResource(R.drawable.arrow_back_fill0),
        contentDescription = "Back arrow",
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(50))
            .clickable{ operation() }
            .padding(5.dp)
    )
}

@Composable
fun Information(content: @Composable ()->Unit){
    Column(modifier = Modifier.fillMaxWidth().padding(start = 5.dp)){
        Icon(painter = painterResource(R.drawable.info_fill0),contentDescription = "info", tint = colorScheme.onBackground.copy(alpha = 0.8F))
        Spacer(Modifier.padding(vertical = 1.dp))
        Row {
            Spacer(Modifier.padding(horizontal = 1.dp))
            content()
        }
    }
}

@Composable
fun RadioButtonItem(
    text:String,
    selected:()->Boolean,
    operation:()->Unit,
    textColor: Color = colorScheme.onBackground
){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = if(isWear){3.dp}else{0.dp})
        .clip(RoundedCornerShape(25))
        .clickable(onClick = operation)
    ) {
        RadioButton(selected = selected(), onClick = operation,modifier=if(isWear){Modifier.size(28.dp)}else{Modifier})
        Text(text = text, style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium}, color = textColor,
            modifier = Modifier.padding(bottom = 2.dp))
    }
}

@Composable
fun CheckBoxItem(
    text:String,
    checked:()->Boolean,
    operation:()->Unit,
    textColor:Color = colorScheme.onBackground
){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = if(isWear){3.dp}else{0.dp})
        .clip(RoundedCornerShape(25))
        .clickable(onClick = operation)
    ) {
        Checkbox(
            checked = checked(),
            onCheckedChange = {operation()},
            modifier=if(isWear){Modifier.size(28.dp)}else{Modifier}
        )
        Text(text = text, style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium}, color = textColor, modifier = Modifier.padding(bottom = 2.dp))
    }
}

@Composable
fun SwitchItem(
    @StringRes title: Int,
    desc: String,
    @DrawableRes icon: Int?,
    getState: ()->Boolean,
    onCheckedChange: (Boolean)->Unit,
    enable:Boolean=true
){
    var checked by remember{mutableStateOf(false)}
    checked = getState()
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterStart)){
            Spacer(Modifier.fillMaxWidth(0.07F))
            if(icon!=null){
                Icon(painter = painterResource(icon),contentDescription = null)
                Spacer(Modifier.fillMaxWidth(0.08F))
            }
            Column(modifier = Modifier.padding(end = 60.dp)){
                Text(text = stringResource(title), style = typography.titleLarge)
                if(desc!=""){Text(text = desc, color = colorScheme.onBackground.copy(alpha = 0.8F))}else{Spacer(Modifier.padding(vertical = 1.dp))}
            }
        }
        Switch(
            checked = checked, onCheckedChange = {onCheckedChange(it);checked=getState()},
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp), enabled = enable
        )
    }
}
