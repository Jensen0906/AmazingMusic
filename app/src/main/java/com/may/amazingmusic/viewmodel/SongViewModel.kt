package com.may.amazingmusic.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.Favorite
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.bean.User
import com.may.amazingmusic.constant.BaseWorkConst.ADD_LIST_AND_PLAY
import com.may.amazingmusic.constant.BaseWorkConst.ADD_LIST_LAST
import com.may.amazingmusic.constant.BaseWorkConst.ADD_LIST_NEXT
import com.may.amazingmusic.constant.NetWorkConst.CONTENT_TYPE
import com.may.amazingmusic.repository.SongRepository
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.utils.player.PlayerManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 *
 * @author May
 * @date 2024/9/16 13:07
 * @description SongViewModel
 */
class SongViewModel : ViewModel() {
    private val TAG = this.javaClass.simpleName

    private val repository = SongRepository()
    var checkGetSongs = false

    val songs = MutableSharedFlow<List<Song>?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun getSongs(page: Int) {
        Log.d(TAG, "getSongs: page=$page")
        checkGetSongs = true
        if (page < 1) {
            return
        }

        val requestBody = "$page".toRequestBody(CONTENT_TYPE.toMediaTypeOrNull())
        viewModelScope.launch {
            repository.getSongs(songs, requestBody)
        }
    }

    val favoriteSongs: MutableLiveData<List<Song>?> = MutableLiveData()
    fun getFavoriteSongs() {
        viewModelScope.launch {
            val uid = DataStoreManager.userIDFlow.first().orZero()
            Log.d(TAG, "getFavoriteSongs: uid=$uid")
            if (uid > 0) {
                val requestBody =
                    Gson().toJson(User().apply { this.uid = uid }).toRequestBody(CONTENT_TYPE.toMediaTypeOrNull())
                repository.getFavoriteSongs(favoriteSongs, requestBody)
            } else {
                favoriteSongs.postValue(emptyList())
            }

        }
    }

    val addSongToPlay = MutableSharedFlow<Map<Song, Int>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    fun addSongToPlaylist(song: Song, playNow: Boolean = false) {
        val position = PlayerManager.playlist.indexOfFirst { it.sid == song.sid }
        Log.d(TAG, "addSongToPlaylist: position=$position, play now=$playNow")
        if (playNow) {
            if (position < 0) addSongToPlay.tryEmit(mapOf(Pair(song, ADD_LIST_AND_PLAY)))
            else addSongToPlay.tryEmit(mapOf(Pair(song, position)))
        } else {
            if (position < 0) addSongToPlay.tryEmit(mapOf(Pair(song, ADD_LIST_NEXT)))
        }
    }

    fun operateFavorite(song: Song, position: Int) {
        viewModelScope.launch {
            val uid = DataStoreManager.userIDFlow.first()
            Log.d(TAG, "operateFavorite: uid=$uid")
            if (uid == null) {
                return@launch
            }
            val requestBody =
                Gson().toJson(Favorite(uid, song.sid)).toRequestBody(CONTENT_TYPE.toMediaTypeOrNull())
            val ops = repository.operateFavorite(requestBody)
            Log.d(TAG, "operateFavorite: ops === $ops, position === $position")
            if (ops) {
                song.isFavorite = !song.isFavorite
                ToastyUtils.success(if (song.isFavorite) "收藏成功" else "取消收藏")
                operateFavoriteSongsIds(song.sid, position, song.isFavorite)
            }
        }
    }

    var dialogShowing = false
    val favoriteSids = MutableSharedFlow<List<Long>?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun getFavoriteIds() {
        viewModelScope.launch {
            val uid = DataStoreManager.userIDFlow.first().orZero()
            if (uid > 0) {
                val requestBody = Gson().toJson(User().apply {
                    this.uid = uid
                }).toRequestBody(CONTENT_TYPE.toMediaTypeOrNull())
                repository.getFavoriteSongsIds(favoriteSids, requestBody)
            } else {
                favoriteSids.tryEmit(emptyList())
            }
        }
    }

    val searchSongs = MutableSharedFlow<List<Song>?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun findSongsByAny(keyword: String?) {
        if (keyword.isNullOrEmpty()) {
            searchSongs.tryEmit(emptyList())
            return
        }
        viewModelScope.launch {
            repository.findSongsByAny(searchSongs, keyword)
        }
    }

    fun addAllSongsToPlaylist() {
        val songs = favoriteSongs.value
        if (songs.isNullOrEmpty()) {
            ToastyUtils.error(appContext.getString(R.string.no_favorite_song))
            return
        }
        PlayerManager.clearPlaylist()
        songs.forEachIndexed { index, song ->
            Log.d(TAG, "addAllSongsToPlaylist: index=$index, song=${song.title}")
            addSongToPlay.tryEmit(mapOf(Pair(song, ADD_LIST_LAST)))
        }
        PlayerManager.playAsRepeatMode()
    }

    val songsChanged = MutableSharedFlow<List<Long>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun notifyFavoriteChanged(sids: List<Long>) {
        if (sids.isEmpty()) return
        songsChanged.tryEmit(sids)
    }

    val operateFavoriteSong = MutableSharedFlow<Triple<Long, Int, Boolean>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private fun operateFavoriteSongsIds(sid: Long, position: Int, isFavorite: Boolean = false) {
        operateFavoriteSong.tryEmit(Triple(sid, position, isFavorite))
    }
}