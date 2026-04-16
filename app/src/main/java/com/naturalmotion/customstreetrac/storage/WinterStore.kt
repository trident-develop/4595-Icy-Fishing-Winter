package com.naturalmotion.customstreetrac.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.winterStore: DataStore<Preferences> by preferencesDataStore(name = "winter_prefs")