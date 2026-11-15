package com.bintianqi.owndroid.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.ui.FeatureEntryItem
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.TopBarSearchTextField
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.adaptiveInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(vm: SearchViewModel, navigateUp: () -> Unit, navigate: (Destination) -> Unit) {
    val query by vm.queryState.collectAsState()
    val results by vm.resultsState.collectAsState()
    val privilege by vm.privilegeState.collectAsState()
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        fr.requestFocus()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                {
                    TopBarSearchTextField(query, vm::setQuery) { vm.setQuery("") }
                },
                navigationIcon = { NavIcon(navigateUp) }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            results.forEach { category ->
                if (category.second.isNotEmpty()) {
                    stickyHeader(category.first) {
                        Column(
                            Modifier
                                .animateItem()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            Text(
                                stringResource(category.first),
                                Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                            HorizontalDivider()
                        }
                    }
                }
                items(category.second, { it.hashCode() }) { feature ->
                    Box(Modifier.animateItem()) {
                        FeatureEntryItem(feature, privilege, navigate)
                    }
                }
            }
            item {
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
}
