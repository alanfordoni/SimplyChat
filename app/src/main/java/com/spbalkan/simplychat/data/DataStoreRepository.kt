package com.spbalkan.simplychat.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val LAST_MESSAGES_SYNC = longPreferencesKey("last_messages_sync")
        val USERNAME = stringPreferencesKey("username")
        val HAS_REQUESTED_NOTIFICATION_PERMISSION = booleanPreferencesKey("has_requested_notification_permission")
    }

    val lastMessagesSync: Flow<Long?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_MESSAGES_SYNC]
        }

    suspend fun saveLastMessagesSync(lastSyncTime: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_MESSAGES_SYNC] = lastSyncTime
        }
    }

    // TODO: move username and other user related data to datastore proto
    val username: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USERNAME]
        }

    suspend fun saveUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = username
        }
    }

    val hasRequestedNotificationPermission: Flow<Boolean?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_REQUESTED_NOTIFICATION_PERMISSION]
        }

    suspend fun saveHasRequestedNotificationPermission(hasRequestedNotificationPermission: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_REQUESTED_NOTIFICATION_PERMISSION] = hasRequestedNotificationPermission
        }
    }

    companion object {
        const val DATA_STORE_NAME = "data_store_simply_chat"
    }
}