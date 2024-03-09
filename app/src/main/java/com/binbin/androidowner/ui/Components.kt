package com.binbin.androidowner.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp


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
