package com.bintianqi.owndroid.ui

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.writeClipBoard
import com.bintianqi.owndroid.zhCN
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SubPageItem(
    @StringRes title: Int,
    desc:String,
    @DrawableRes icon: Int? = null,
    operation: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = operation).padding(top = 15.dp, bottom = 15.dp, start = 30.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(icon != null) {
            Icon(painter = painterResource(icon), contentDescription = stringResource(title), modifier = Modifier.padding(top = 1.dp))
            Spacer(Modifier.padding(start = 15.dp))
        }
        Column {
            Text(text = stringResource(title), style = typography.titleLarge, modifier = Modifier.padding(bottom = if(zhCN) { 2 } else { 0 }.dp))
            if(desc!="") { Text(text = desc, color = colorScheme.onBackground.copy(alpha = 0.8F)) }
        }
    }
}

@Composable
fun NavIcon(operation: () -> Unit) {
    Icon(
        painter = painterResource(R.drawable.arrow_back_fill0),
        contentDescription = "Back arrow",
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(50))
            .clickable(onClick = operation)
            .padding(5.dp)
    )
}

@Composable
fun Information(content: @Composable ()->Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 5.dp, top = 20.dp)) {
        Icon(
            painter = painterResource(R.drawable.info_fill0),
            contentDescription = "info",
            tint = colorScheme.onBackground.copy(alpha = 0.8F)
        )
        Spacer(Modifier.padding(vertical = 1.dp))
        Column(modifier = Modifier.padding(start = 2.dp)) {
            content()
        }
    }
}

@Composable
fun RadioButtonItem(
    @StringRes text: Int,
    selected: Boolean,
    operation: () -> Unit,
    textColor: Color = colorScheme.onBackground
) {
    RadioButtonItem(stringResource(text), selected, operation, textColor)
}

@Composable
fun RadioButtonItem(
    text: String,
    selected: Boolean,
    operation: () -> Unit,
    textColor: Color = colorScheme.onBackground
) {
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(25))
        .clickable(onClick = operation)
    ) {
        RadioButton(selected = selected, onClick = operation)
        Text(text = text, color = textColor, modifier = Modifier.padding(bottom = if(zhCN) { 2 } else { 0 }.dp))
    }
}

@Composable
fun CheckBoxItem(
    @StringRes text: Int,
    checked: Boolean,
    operation: (Boolean) -> Unit,
    textColor: Color = colorScheme.onBackground
) {
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(25))
        .clickable { operation(!checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = operation
        )
        Text(text = stringResource(text), color = textColor, modifier = Modifier.padding(bottom = if(zhCN) { 2 } else { 0 }.dp))
    }
}


@Composable
fun SwitchItem(
    @StringRes title: Int,
    desc: String,
    @DrawableRes icon: Int?,
    getState: ()->Boolean,
    onCheckedChange: (Boolean)->Unit,
    enable: Boolean = true,
    onClickBlank: (() -> Unit)? = null,
    padding: Boolean = true
) {
    var state by remember { mutableStateOf(getState()) }
    SwitchItem(title, desc, icon, state, { onCheckedChange(it); state = getState() }, enable, onClickBlank, padding)
}

@Composable
fun SwitchItem(
    @StringRes title: Int,
    desc: String,
    @DrawableRes icon: Int?,
    state: Boolean,
    onCheckedChange: (Boolean)->Unit,
    enable: Boolean = true,
    onClickBlank: (() -> Unit)? = null,
    padding: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClickBlank != null, onClick = onClickBlank?:{})
            .padding(top = 5.dp, bottom = 5.dp, start = if(padding) 30.dp else 0.dp, end = if(padding) 12.dp else 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            if(icon != null) {
                Icon(painter = painterResource(icon),contentDescription = null)
                Spacer(Modifier.padding(start = 15.dp))
            }
            Column(modifier = Modifier.padding(end = 60.dp)) {
                Text(text = stringResource(title), style = typography.titleLarge)
                if(desc!="") {
                    Text(text = desc, color = colorScheme.onBackground.copy(alpha = 0.8F))
                }
                if(zhCN) { Spacer(Modifier.padding(vertical = 1.dp)) }
            }
        }
        Switch(
            checked = state, onCheckedChange = { onCheckedChange(it) },
            modifier = Modifier.align(Alignment.CenterEnd), enabled = enable
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    backStackEntry: NavBackStackEntry?,
    navCtrl: NavHostController,
    localNavCtrl: NavHostController,
    title: @Composable ()->Unit = {}
) {
    TopAppBar(
        title = title,
        navigationIcon = {
            NavIcon{
                if(backStackEntry?.destination?.route == "Home") { navCtrl.navigateUp() } else { localNavCtrl.navigateUp() }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background)
    )
}

@Composable
fun CopyTextButton(@StringRes label: Int, content: String) {
    val context = LocalContext.current
    var ok by remember{ mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Button(
        onClick = {
            if(!ok) {
                scope.launch {
                    if(writeClipBoard(context,content)) { ok = true; delay(2000); ok = false }
                    else{ Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show() }
                }
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.animateContentSize()
        ) {
            Icon(painter = painterResource(if(ok) R.drawable.check_fill0 else R.drawable.content_copy_fill0), contentDescription = null)
            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            Text(text = stringResource(if(ok) R.string.success else label))
        }
    }
}

@Composable
fun CardItem(@StringRes title: Int, @StringRes text: Int) {
    CardItem(title, stringResource(text))
}

@Composable
fun CardItem(@StringRes title: Int, text: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(text = stringResource(title), style = typography.titleLarge, modifier = Modifier.padding(start = 8.dp, top = 6.dp))
        SelectionContainer {
            Text(text = text, modifier = Modifier.padding(start = 8.dp, bottom = 6.dp))
        }
    }
}

@Composable
fun ListItem(text: String, onDelete: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(15)).background(colorScheme.surfaceVariant)
    ) {
        Text(text = text, modifier = Modifier.padding(start = 12.dp))
        IconButton(
            onClick = onDelete
        ) {
            Icon(
                painter = painterResource(R.drawable.close_fill0),
                contentDescription = stringResource(R.string.delete)
            )
        }
    }
}
