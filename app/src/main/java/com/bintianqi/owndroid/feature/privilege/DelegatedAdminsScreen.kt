package com.bintianqi.owndroid.feature.privilege

import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.PackageNameTextField
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.channels.Channel

@RequiresApi(26)
@Composable
fun DelegatedAdminsScreen(
    vm: DelegatedAdminsViewModel,
    onNavigateUp: () -> Unit, onNavigate: (Destination.DelegatedAdminDetails) -> Unit
) {
    val admins by vm.delegatedAdminsState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.getDelegatedAdmins() }
    MyLazyScaffold(R.string.delegated_admins, onNavigateUp) {
        itemsIndexed(admins, { _, it -> it.app.name }) { index, admin ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .animateItem(),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        rememberDrawablePainter(admin.app.icon), null,
                        Modifier
                            .padding(start = 12.dp, end = 18.dp)
                            .size(40.dp)
                    )
                    Column {
                        Text(admin.app.label)
                        Text(admin.app.name, Modifier.alpha(0.8F), style = typography.bodyMedium)
                    }
                }
                IconButton({
                    vm.selectedDelegatedAdminIndex = index
                    onNavigate(Destination.DelegatedAdminDetails)
                }) {
                    Icon(Icons.Outlined.Edit, null)
                }
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        vm.selectedDelegatedAdminIndex = -1
                        onNavigate(Destination.DelegatedAdminDetails)
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, null, Modifier.padding(end = 12.dp))
                Text(stringResource(R.string.add_delegated_admin), style = typography.titleMedium)
            }
        }
    }
}

@RequiresApi(26)
@Composable
fun AddDelegatedAdminScreen(
    vm: DelegatedAdminsViewModel, chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val adminsList by vm.delegatedAdminsState.collectAsState()
    val origin = adminsList.getOrNull(vm.selectedDelegatedAdminIndex)
    val updateMode = origin != null
    var input by rememberSaveable { mutableStateOf(origin?.app?.name ?: "") }
    val scopes = rememberSaveable {
        mutableStateListOf(*(origin?.scopes?.toTypedArray() ?: emptyArray()))
    }
    LaunchedEffect(Unit) {
        input = chosenPackage.receive()
    }
    MySmallTitleScaffold(
        if (updateMode) R.string.place_holder else R.string.add_delegated_admin, onNavigateUp, 0.dp
    ) {
        if (updateMode) {
            OutlinedTextField(
                input, {},
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 8.dp),
                readOnly = true, label = { Text(stringResource(R.string.package_name)) }
            )
        } else {
            PackageNameTextField(
                input, onChoosePackage,
                Modifier.padding(HorizontalPadding, 8.dp)
            ) { input = it }
        }
        delegatedScopesList.forEach { scope ->
            val checked = scope.id in scopes
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { if (!checked) scopes += scope.id else scopes -= scope.id }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked, { if (it) scopes += scope.id else scopes -= scope.id },
                    Modifier.padding(horizontal = 4.dp)
                )
                Column {
                    Text(stringResource(scope.string))
                    Text(
                        scope.id, style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Button(
            {
                vm.setDelegatedAdmin(input, scopes)
                onNavigateUp()
            },
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, vertical = 4.dp),
            input.isNotBlank()
        ) {
            Text(stringResource(if (updateMode) R.string.update else R.string.add))
        }
        if (updateMode) Button(
            {
                vm.setDelegatedAdmin(input, emptyList())
                onNavigateUp()
            },
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding),
            colors = ButtonDefaults.buttonColors(colorScheme.error, colorScheme.onError)
        ) {
            Text(stringResource(R.string.delete))
        }
        Spacer(Modifier.height(BottomPadding))
    }
}
