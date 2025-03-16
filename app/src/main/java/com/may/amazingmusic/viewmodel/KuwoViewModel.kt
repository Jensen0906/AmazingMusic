package com.may.amazingmusic.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.may.amazingmusic.bean.Banner
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.bean.SongListInfo
import com.may.amazingmusic.bean.User
import com.may.amazingmusic.constant.NetWorkConst
import com.may.amazingmusic.repository.KuwoRepository
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.ToastyUtils
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
                repository.getKuwoSongRids(myKuwoSongRids, requestBody)
            } else {
                myKuwoSongRids.tryEmit(emptyList())
            }
        }
    }

    fun operateFavorite(song: KuwoSong, position: Int) {
        viewModelScope.launch {
            val uid = DataStoreManager.userIDFlow.first()
            if (uid == null || uid < 0) {
                ToastyUtils.info("请先登录！")
                return@launch
            }
            val requestBody =
                Gson().toJson(song.apply { this.uid = uid }).toRequestBody(NetWorkConst.CONTENT_TYPE.toMediaTypeOrNull())
            val success = repository.operateFavoriteKuwoSong(requestBody)
            if (success) {
                song.isFavorite = !song.isFavorite
                ToastyUtils.success(if (song.isFavorite) "收藏成功" else "取消收藏")
                operateKuwoSongIds(song.rid, position, song.isFavorite)
            }
        }
    }

    val currentLrc = MutableLiveData("")
    fun getKuwoLrc(rid: Long) {
        Log.e(TAG, "getKuwoLrc: ")
        viewModelScope.launch {
            repository.getLrc(currentLrc, rid)
        }
    }

    val operateFavoriteSong = MutableSharedFlow<Triple<Long, Int, Boolean>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private fun operateKuwoSongIds(rid: Long, position: Int, isFavorite: Boolean = false) {
        operateFavoriteSong.tryEmit(Triple(rid, position, isFavorite))
    }

    val banners = MutableSharedFlow<List<Banner>?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun getBanners() {
        Log.e(TAG, "getBanners: ")
        viewModelScope.launch {
            repository.getBanners(banners)
        }
    }

    val songListId = MutableLiveData(-1L)
    val songListInfo = MutableSharedFlow<SongListInfo?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    fun getSongListInfo() {
        viewModelScope.launch {
            val id = songListId.value
            if (id == null || id < 0L) {
                songListInfo.tryEmit(null)
                return@launch
            }
            repository.getSongListInfo(songListInfo, id)
        }
    }
}