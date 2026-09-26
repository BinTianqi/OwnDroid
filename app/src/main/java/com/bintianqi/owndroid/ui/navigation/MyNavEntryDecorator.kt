package com.bintianqi.owndroid.ui.navigation

/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner

const val TAG = "MyNavEntryDecorator"

@Composable
fun <T : Any> rememberSharedViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    removeViewModelStoreOnPop: () -> Boolean = { true },
): SharedViewModelStoreNavEntryDecorator<T> {
    val currentRemoveViewModelStoreOnPop = rememberUpdatedState(removeViewModelStoreOnPop)
    return remember(viewModelStoreOwner, currentRemoveViewModelStoreOnPop) {
        SharedViewModelStoreNavEntryDecorator(
            viewModelStoreOwner.viewModelStore,
            removeViewModelStoreOnPop,
        )
    }
}

class SharedViewModelStoreNavEntryDecorator<T : Any>(
    viewModelStore: ViewModelStore,
    removeViewModelStoreOnPop: () -> Boolean,
) :
    NavEntryDecorator<T>(
        onPop = ({ key ->
            Log.d(TAG, "Popping $key")
            if (removeViewModelStoreOnPop()) {
                viewModelStore.getEntryViewModel().clearViewModelStoreOwnerForKey(key.toString())
            }
        }),
        decorate = { entry ->
            LaunchedEffect(Unit) {
                Log.d(
                    TAG, "Decorating entry, key: ${entry.contentKey}, metadata: ${entry.metadata}"
                )
            }
            // If the entry indicates it has a parent, use its parent's ViewModelStore.
            val parentKey = entry.metadata[PARENT_CONTENT_KEY] as String?
            val contentKey = parentKey ?: (entry.contentKey as String)
            val viewModelStore = viewModelStore.getEntryViewModel().viewModelStoreForKey(contentKey)
            val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
            val childViewModelStoreOwner = remember {
                object :
                    ViewModelStoreOwner,
                    SavedStateRegistryOwner by savedStateRegistryOwner,
                    HasDefaultViewModelProviderFactory {
                    override val viewModelStore: ViewModelStore
                        get() = viewModelStore

                    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
                        get() = SavedStateViewModelFactory()

                    override val defaultViewModelCreationExtras: CreationExtras
                        get() =
                            MutableCreationExtras().also {
                                it[SAVED_STATE_REGISTRY_OWNER_KEY] = this
                                it[VIEW_MODEL_STORE_OWNER_KEY] = this
                            }

                    init {
                        require(this.lifecycle.currentState == Lifecycle.State.INITIALIZED)
                        enableSavedStateHandles()
                    }
                }
            }
            CompositionLocalProvider(LocalViewModelStoreOwner provides childViewModelStoreOwner) {
                entry.Content()
            }
        },
    ) {

    companion object {

        const val PARENT_CONTENT_KEY = "shared_decorator_parent_content_key"


    }

}

inline fun <reified T: Destination> navParentKey() =
    mapOf(SharedViewModelStoreNavEntryDecorator.PARENT_CONTENT_KEY to T::class.simpleName!!)

private class EntryViewModel : ViewModel() {
    private val owners = mutableMapOf<String, ViewModelStore>()

    fun viewModelStoreForKey(key: String): ViewModelStore {
        Log.d(TAG, "Get ViewModelStore for key $key")
        val result = owners.getOrPut(key) { ViewModelStore() }
        Log.d(TAG, "EntryViewModel owners: $owners")
        return result
    }

    fun clearViewModelStoreOwnerForKey(key: String) {
        Log.d(TAG, "Clear ViewModelStore for key $key")
        owners.remove(key)?.clear()
    }

    override fun onCleared() {
        owners.forEach { (_, store) -> store.clear() }
    }
}


private fun ViewModelStore.getEntryViewModel(): EntryViewModel {
    val provider =
        ViewModelProvider.create(
            store = this,
            factory = viewModelFactory { initializer { EntryViewModel() } },
        )
    return provider[EntryViewModel::class]
}
