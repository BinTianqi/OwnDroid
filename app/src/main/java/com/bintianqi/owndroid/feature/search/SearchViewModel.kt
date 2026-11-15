package com.bintianqi.owndroid.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.feature.FeatureEntries
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.searchInString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    val application: MyApplication,
    val privilegeState: StateFlow<PrivilegeStatus>,
    val settingsRepo: SettingsRepository,
) : ViewModel() {

    fun getShowUnavailableFeatures(): Boolean {
        return settingsRepo.data.showUnavailableFeatures
    }

    val queryState = MutableStateFlow("")

    fun setQuery(query: String) {
        queryState.value = query
    }

    val resultsState = MutableStateFlow(FeatureEntries)

    init {
        viewModelScope.launch {
            queryState.collect {
                updateSearchResults(it)
            }
        }
    }

    fun updateSearchResults(query: String) {
        if (query.isNotBlank()) {
            resultsState.value = FeatureEntries.mapNotNull { category ->
                val features = category.second.filter { feature ->
                    (feature.checkAvailable(privilegeState.value) ||
                            getShowUnavailableFeatures()) &&
                            (feature.keywords + feature.name).any {
                        searchInString(query, application.getString(it))
                    }
                }
                if (features.isEmpty()) null else (category.first to features)
            }
        } else {
            resultsState.value = FeatureEntries
        }
    }
}