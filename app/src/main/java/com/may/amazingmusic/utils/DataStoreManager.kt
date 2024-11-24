package com.may.amazingmusic.utils

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.bean.User
import com.may.amazingmusic.userDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreManager {
    private val USER_UID_KEY = intPreferencesKey("user_uid")
    private val USER_USERNAME_KEY = stringPreferencesKey("user_username")
    private val USER_PASSWORD_KEY = stringPreferencesKey("user_password")
    private val USER_STATUS_KEY = intPreferencesKey("user_status")

    private val PLAYER_REPEAT_MODE = intPreferencesKey("repeat_mode")

    private val TIMER_OPENED = booleanPreferencesKey("timer_opened")
    private val STOP_UNTIL_THIS_SONG_FINISH = booleanPreferencesKey("stop_until_this_song_finish")

    suspend fun saveUserInfo(user: User) {
        saveUserID(user.uid)
        user.username?.let { saveUsername(it) }
        user.password?.let { savePassword(it) }
        saveUserStatus(user.userStatus)
    }

    private suspend fun saveUserID(uid: Int) {
        appContext.userDataStore.edit { it[USER_UID_KEY] = uid }
    }

    private suspend fun saveUsername(username: String) {
        appContext.userDataStore.edit { it[USER_USERNAME_KEY] = username }
    }

    private suspend fun savePassword(password: String) {
        appContext.userDataStore.edit { it[USER_PASSWORD_KEY] = password }
    }

    private suspend fun saveUserStatus(uid: Int) {
        appContext.userDataStore.edit { it[USER_STATUS_KEY] = uid }
    }

    suspend fun saveRepeatMode(repeatMode: Int) {
        appContext.userDataStore.edit { it[PLAYER_REPEAT_MODE] = repeatMode }
    }

    suspend fun updateTimerOpened(isOpen: Boolean) {
        appContext.userDataStore.edit { it[TIMER_OPENED] = isOpen }
    }

    suspend fun updateStopUntilPlayCompleted(wait: Boolean) {
        appContext.userDataStore.edit { it[STOP_UNTIL_THIS_SONG_FINISH] = wait }
    }

    suspend fun deleteUserID() {
        appContext.userDataStore.edit { it.remove(USER_UID_KEY) }
    }

    val userIDFlow: Flow<Int?> = appContext.userDataStore.data.map { it[USER_UID_KEY] }

    val userUsernameFlow: Flow<String?> = appContext.userDataStore.data.map { it[USER_USERNAME_KEY] }

    val userPasswordFlow: Flow<String?> = appContext.userDataStore.data.map { it[USER_PASSWORD_KEY] }

    val userStatusFlow: Flow<Int?> = appContext.userDataStore.data.map { it[USER_STATUS_KEY] }

    val repeatModeFlow: Flow<Int?> = appContext.userDataStore.data.map { it[PLAYER_REPEAT_MODE] }

    val timerOpenedFlow: Flow<Boolean?> = appContext.userDataStore.data.map { it[TIMER_OPENED] }

    val stopUntilPlayCompleted: Flow<Boolean?> = appContext.userDataStore.data.map { it[STOP_UNTIL_THIS_SONG_FINISH] }
}