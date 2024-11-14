package com.may.amazingmusic.repository

import androidx.lifecycle.MutableLiveData
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.utils.base.BaseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.RequestBody

/**
 *
 * @author May
 * @date 2024/9/16 13:08
 * @description SongRepository
 */
class SongRepository : BaseRepository() {

    suspend fun getSongs(songs: MutableSharedFlow<List<Song>?>, requestBody: RequestBody) {
        execute({ api.getSongs(requestBody) }, songs)
    }

    suspend fun findSongsByAny(songs: MutableSharedFlow<List<Song>?>, requestBody: String) {
        execute({ api.findSongsByAny(requestBody) }, songs)
    }

    suspend fun getFavoriteSongs(songs: MutableLiveData<List<Song>?>, requestBody: RequestBody) {
        execute({ api.getFavoriteSongs(requestBody) }, songs)
    }

    suspend fun getFavoriteSongsIds(sids: MutableSharedFlow<List<Long>?>, requestBody: RequestBody) {
        execute({ api.getFavoriteSongsIds(requestBody) }, sids)
    }

    suspend fun operateFavorite(requestBody: RequestBody): Boolean {
        return executeResult({ api.operateFavorite(requestBody) })
    }
}