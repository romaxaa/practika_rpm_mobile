package com.example.collegeschedule.data.favorites

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Создаём DataStore для Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

class FavoritesStore(private val context: Context) {

    private val FAVORITES_KEY = stringSetPreferencesKey("favorite_groups")

    val favorites: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[FAVORITES_KEY] ?: emptySet()
    }

    suspend fun toggle(group: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITES_KEY]?.toMutableSet() ?: mutableSetOf()

            if (current.contains(group)) {
                current.remove(group)
            } else {
                current.add(group)
            }

            preferences[FAVORITES_KEY] = current
        }
    }
}
