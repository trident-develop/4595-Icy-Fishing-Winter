package com.naturalmotion.customstreetrac.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class ScoreRepoImpl(
    private val dataStore: DataStore<Preferences>
) : ScoreRepo {

    private val key = stringPreferencesKey("saved_score")
    private val notifyShownKey = booleanPreferencesKey("notification_request_shown")

    override suspend fun getSavedScore(): String? {
        val prefs = dataStore.data.first()
        return prefs[key]
    }

    override suspend fun saveScore(book: String) {
        val prefs = dataStore.data.first()
        val current = prefs[key]

        if (current == book) {
//            Log.d("MYTAG", "LINK ALREADY SAVED -> skip")
            return
        }

//        Log.d("MYTAG", "SAVE LINK -> $book")

        dataStore.edit { mutablePrefs ->
            mutablePrefs[key] = book
        }
    }

    override suspend fun isNotifyShown(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[notifyShownKey] ?: false
    }

    override suspend fun markNotifyShown() {
        dataStore.edit { prefs ->
            prefs[notifyShownKey] = true
        }
    }
}