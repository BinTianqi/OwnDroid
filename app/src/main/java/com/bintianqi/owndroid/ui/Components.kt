package com.bintianqi.owndroid.ui

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.writeClipBoard
import com.bintianqi.owndroid.zhCN
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FunctionItem(
    @StringRes title: Int,
    desc: String? = null,
    @DrawableRes icon: Int? = null,
    operation: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = operation)
            .padding(start = 25.dp, end = 15.dp)
            .padding(vertical = 12.dp + (if(desc != "") 0 else 3).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(icon != null) Icon(
            painter = painterResource(icon), contentDescription = null,
            modifier = Modifier.padding(top = 1.dp, end = 20.dp).offset(x = (-2).dp)
        )
        Column {
            Text(
                text = stringResource(title),
                style = typography.titleLarge,
                modifier = Modifier.padding(bottom = if(zhCN) 2.dp else 0.dp)
            )
            if(desc != null) { Text(text = desc, color = colorScheme.onBackground.copy(alpha = 0.8F)) }
        }
    }
}

@Composable
fun NavIcon(onClick: () -> Unit) {
    IconButton(onClick) {
        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
    }
}

@Composable
fun RadioButtonItem(
    @StringRes text: Int,
    selected: Boolean,
    operation: () -> Unit
) {
    RadioButtonItem(stringResource(text), selected, operation)
}

@Composable
fun RadioButtonItem(
    text: String,
    selected: Boolean,
    operation: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(25))
        .clickable(onClick = operation)
    ) {
        RadioButton(selected = selected, onClick = operation)
        Text(text = text, modifier = Modifier.padding(bottom = if(zhCN) { 2 } else { 0 }.dp))
    }
}

@Composable
fun FullWidthRadioButtonItem(
    text: Int,
    selected: Boolean,
    operation: () -> Unit
) = FullWidthRadioButtonItem(stringResource(text), selected, operation)

@Composable
fun FullWidthRadioButtonItem(
    text: String,
    selected: Boolean,
    operation: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = operation)
    ) {
        RadioButton(selected = selected, onClick = operation, modifier = Modifier.padding(horizontal = 4.dp))
        Text(text = text, modifier = Modifier.padding(bottom = if(zhCN) { 2 } else { 0 }.dp))
    }
}

@Composable
fun CheckBoxItem(
    @StringRes text: Int,
    checked: Boolean,
    operation: (Boolean) -> Unit
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
        Text(text = stringResource(text), modifier = Modifier.padding(bottom = if(zhCN) { 2 } else { 0 }.dp))
    }
}

@Composable
fun FullWidthCheckBoxItem(
    @StringRes text: Int,
    checked: Boolean,
    operation: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { operation(!checked) }
    ) {
        Checkbox(checked = checked, onCheckedChange = operation, modifier = Modifier.padding(horizontal = 4.dp))
        Text(text = stringResource(text), modifier = Modifier.padding(bottom = if(zhCN) { 2 } else { 0 }.dp))
    }
}

@Composable
fun SwitchItem(
    @StringRes title: Int,
    desc: String? = null,
    @DrawableRes icon: Int? = null,
    getState: () -> Boolean,
    onCheckedChange: (Boolean)->Unit,
    enabled: Boolean = true,
    onClickBlank: (() -> Unit)? = null,
    padding: Boolean = true
) {
    var state by remember { mutableStateOf(getState()) }
    SwitchItem(title, desc, icon, state, { onCheckedChange(it); state = getState() }, enabled, onClickBlank, padding)
}

@Composable
fun SwitchItem(
    @StringRes title: Int,
    desc: String? = null,
    @DrawableRes icon: Int? = null,
    state: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    onClickBlank: (() -> Unit)? = null,
    padding: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClickBlank != null, onClick = onClickBlank?:{})
            .padding(start = if(padding) 25.dp else 0.dp, end = if(padding) 15.dp else 0.dp, top = 5.dp, bottom = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            if(icon != null) Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.padding(end = 20.dp).offset(x = (-2).dp)
            )
            Column(modifier = Modifier.padding(end = 60.dp, bottom = if(zhCN) 2.dp else 0.dp)) {
                Text(text = stringResource(title), style = typography.titleLarge)
                if(desc != null) Text(text = desc, color = colorScheme.onBackground.copy(alpha = 0.8F))
            }
        }
        Switch(
            checked = state, onCheckedChange = { onCheckedChange(it) },
            modifier = Modifier.align(Alignment.CenterEnd), enabled = enabled
        )
    }
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
fun InfoItem(title: Int, text: Int, withInfo: Boolean = false, onClick: () -> Unit = {}) =
    InfoItem(title, stringResource(text), withInfo, onClick)

@Composable
fun InfoItem(title: Int, text: String, withInfo: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).padding(start = HorizontalPadding, end = 8.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Column {
            Text(stringResource(title), style = typography.titleLarge)
            Text(text, Modifier.alpha(0.8F))
        }
        if(withInfo) IconButton(onClick) { Icon(Icons.Outlined.Info, null) }
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

@Composable
fun Notes(@StringRes strID: Int, horizonPadding: Dp = 0.dp) {
    Icon(Icons.Outlined.Info, null, Modifier.padding(horizontal = horizonPadding).padding(top = 4.dp, bottom = 8.dp), colorScheme.onSurfaceVariant)
    Text(
        stringResource(strID), Modifier.padding(horizontal = horizonPadding),
        color = colorScheme.onSurfaceVariant, style = typography.bodyMedium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScaffold(
    @StringRes title: Int,
    onNavIconClicked: () -> Unit,
    horizonPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(title)) },
                navigationIcon = { NavIcon(onNavIconClicked) },
                scrollBehavior = sb
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = horizonPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLazyScaffold(
    @StringRes title: Int,
    onNavIconClicked: () -> Unit,
    content: LazyListScope.() -> Unit
) {
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(title)) },
                navigationIcon = { NavIcon(onNavIconClicked) },
                scrollBehavior = sb
            )
        }
    ) { paddingValues ->
        LazyColumn(Modifier.fillMaxSize().padding(paddingValues), content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySmallTitleScaffold(
    @StringRes title: Int,
    onNavIconClicked: () -> Unit,
    horizonPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(title)) },
                navigationIcon = { NavIcon(onNavIconClicked) },
                colors = TopAppBarDefaults.topAppBarColors(colorScheme.surfaceContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = horizonPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ExpandExposedTextFieldIcon(active: Boolean) {
    val degrees by animateFloatAsState(if(active) 180F else 0F)
    Icon(
        imageVector = Icons.Default.ArrowDropDown, contentDescription = null,
        modifier = Modifier.rotate(degrees)
    )
}

@Composable
fun ErrorDialog(message: String?, onDismiss: () -> Unit) {
    if(!message.isNullOrEmpty()) AlertDialog(
        title = { Text(stringResource(R.string.error)) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onDismiss) { Text(stringResource(R.string.confirm)) }
        },
        onDismissRequest = onDismiss
    )
}
