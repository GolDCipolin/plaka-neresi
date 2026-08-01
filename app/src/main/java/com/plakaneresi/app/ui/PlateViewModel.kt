package com.plakaneresi.app.ui

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.plakaneresi.app.plates.PlateSearch
import com.plakaneresi.app.plates.Province
import com.plakaneresi.app.plates.Provinces
import com.plakaneresi.app.plates.SearchHit

/** How the full list is ordered when nothing is being searched. */
enum class BrowseSort { CODE, NAME }

class PlateViewModel : ViewModel() {

    var query by mutableStateOf("")
        private set

    var sort by mutableStateOf(BrowseSort.CODE)
        private set

    /** Search results. [derivedStateOf] keeps this off the recomposition hot path. */
    val hits: List<SearchHit> by derivedStateOf { PlateSearch.query(query) }

    val browseList: List<Province> by derivedStateOf {
        when (sort) {
            BrowseSort.CODE -> Provinces.all
            BrowseSort.NAME -> Provinces.alphabetical
        }
    }

    val isSearching: Boolean by derivedStateOf { query.isNotBlank() }

    fun onQueryChange(value: String) {
        query = value
    }

    fun onClear() {
        query = ""
    }

    fun onSortChange(value: BrowseSort) {
        sort = value
    }
}
