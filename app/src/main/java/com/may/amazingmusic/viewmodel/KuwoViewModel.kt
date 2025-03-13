package com.may.amazingmusic.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.bean.User
import com.may.amazingmusic.constant.NetWorkConst
import com.may.amazingmusic.repository.KuwoRepository
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.orZero
import com.may.amazingmusic.utils.player.PlayerManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * @Author Jensen
 * @Date 2025/3/9 11:34
 */
class KuwoViewModel : ViewModel() {
    private val TAG = this.javaClass.simpleName

    private val repository = KuwoRepository()
    private var keyword = ""


    val searchSongs = MutableSharedFlow<List<KuwoSong>?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    fun searchSongs(keyword: String?) {
        if (keyword.isNullOrEmpty() || PlayerManager.kuwoPage >= 10) {
            searchSongs.tryEmit(emptyList())
            return
        }
        this.keyword = keyword
        Log.e(TAG, "searchSongs: keyword=$keyword, page=${PlayerManager.kuwoPage}")
        viewModelScope.launch {
            repository.searchSongs(searchSongs, keyword, PlayerManager.kuwoPage, 10)
        }
    }

    val isKuwoSource = MutableLiveData(false)
    fun isKuwoSource() {
        viewModelScope.launch {
            DataStoreManager.isKuwoSelected.collect {
                isKuwoSource.postValue(it.isTrue())
            }
        }
    }

    val myKuwoSongs = MutableSharedFlow<List<KuwoSong>?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    fun getMyKuwoSongs() {
        viewModelScope.launch {
            val uid = DataStoreManager.userIDFlow.first().orZero()
            if (uid > 0) {
                val requestBody = Gson().toJson(User().apply {
                    this.uid = uid
                }).toRequestBody(NetWorkConst.CONTENT_TYPE.toMediaTypeOrNull())
                Log.e(TAG, "getMyKuwoSongs: requestBody=$requestBody")
                repository.getFavoriteKuwoSongs(myKuwoSongs, requestBody)
            } else {
                myKuwoSongs.tryEmit(emptyList())
            }
        }
    }

    val myKuwoSongRids = MutableSharedFlow<List<Long>?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    fun getMyKuwoSongRids() {
        viewModelScope.launch {
            val uid = DataStoreManager.userIDFlow.first().orZero()
            if (uid > 0) {
                val requestBody = Gson().toJson(User().apply {
                    this.uid = uid
                }).toRequestBody(NetWorkConst.CONTENT_TYPE.toMediaTypeOrNull())
                Log.e(TAG, "getMyKuwoSongs: requestBody=$requestBody")
                repository.getKuwoSongRids(myKuwoSongRids, requestBody)
            } else {
                myKuwoSongs.tryEmit(emptyList())
            }
        }
    }

//    fun addAllSongsToPlaylist() {
//        viewModelScope.launch {
//            val songs = myKuwoSongs.first()
//            if (songs.isNullOrEmpty()) {
//                ToastyUtils.error(App.appContext.getString(R.string.no_favorite_song))
//                return@launch
//            }
//            PlayerManager.clearPlaylist()
//            songs.forEachIndexed { index, song ->
//                Log.d(TAG, "addAllSongsToPlaylist: index=$index, song=${song.title}")
//                addSongToPlay.tryEmit(mapOf(Pair(song, BaseWorkConst.ADD_LIST_LAST)))
//            }
//            PlayerManager.playAsRepeatMode()
//        }
//
//    }

}