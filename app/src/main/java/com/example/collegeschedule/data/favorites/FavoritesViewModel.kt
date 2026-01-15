package com.example.collegeschedule.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collegeschedule.data.favorites.FavoritesStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val store: FavoritesStore
) : ViewModel() {

    val favorites = store.favorites.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptySet()
    )

    fun toggle(group: String) {
        viewModelScope.launch {
            store.toggle(group)
        }
    }
}
