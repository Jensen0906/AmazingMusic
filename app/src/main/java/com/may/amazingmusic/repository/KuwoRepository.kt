package com.may.amazingmusic.repository

import androidx.lifecycle.MutableLiveData
import com.may.amazingmusic.bean.Banner
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.bean.SongList
import com.may.amazingmusic.bean.SongListInfo
import com.may.amazingmusic.constant.NetWorkConst
import com.may.amazingmusic.utils.RetrofitService
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.RequestBody

/**
 * @Author Jensen
 * @Date 2025/3/9 11:36
 */
class KuwoRepository {
    private val TAG = this.javaClass.simpleName

    private val kuwoApi = RetrofitService.getKuwoApi()
    private val baseApi = RetrofitService.getApi()

    suspend fun searchSongs(songs: MutableSharedFlow<List<KuwoSong>?>, keyword: String, page: Int, limit: Int) {
        runCatching {
            val result = kuwoApi.searchSongResult(keyword, page, limit)
            songs.tryEmit(result.data)
        }.onFailure {
            it.printStackTrace()
            songs.tryEmit(null)
        }
    }

    suspend fun getFavoriteKuwoSongs(kuwoSongs: MutableSharedFlow<List<KuwoSong>?>, requestBody: RequestBody) {
        runCatching {
            val result = baseApi.getFavoriteKuwoSongs(requestBody)
            kuwoSongs.tryEmit(result.data)
        }.onFailure {
            it.printStackTrace()
            kuwoSongs.tryEmit(null)
        }
    }

    suspend fun operateFavoriteKuwoSong(requestBody: RequestBody): Boolean {
        runCatching {
            val result = baseApi.operateFavoriteKuwoSong(requestBody)
            return result.code == NetWorkConst.SUCCESS_STATUS
        }.onFailure {
            it.printStackTrace()
        }
        return false
    }

    suspend fun getKuwoSongRids(kuwoSongRids: MutableSharedFlow<List<Long>?>, requestBody: RequestBody) {
        runCatching {
            val result = baseApi.getKuwoSongRids(requestBody)
            kuwoSongRids.tryEmit(result.data)
        }.onFailure {
            it.printStackTrace()
            kuwoSongRids.tryEmit(null)
        }
    }


    suspend fun getLrc(lrc: MutableLiveData<String?>, rid: Long) {
        runCatching {
            val result = kuwoApi.getLrc(rid, "lyr")
            lrc.postValue(result.data?.lrclist)
        }.onFailure {
            it.printStackTrace()
            lrc.postValue(null)
        }
    }

    suspend fun getBanners(banners: MutableSharedFlow<List<Banner>?>) {
        runCatching {
            val result = kuwoApi.getBanner()
            banners.tryEmit(result.data?.banners)
        }.onFailure {
            it.printStackTrace()
            banners.tryEmit(null)
        }
    }

    suspend fun getSongListInfo(songListInfo: MutableSharedFlow<SongListInfo?>, songListId: Long, page: Int = 1) {
        runCatching {
            songListInfo.tryEmit(kuwoApi.getSongListInfo(songListId = songListId, page = page).data)
        }.onFailure {
            it.printStackTrace()
            songListInfo.tryEmit(null)
        }
    }

    suspend fun getSongLists(songLists: MutableSharedFlow<List<SongList>?>, page: Int = 1) {
        runCatching {
            songLists.tryEmit(kuwoApi.getSongLists(page = page).data)
        }.onFailure {
            it.printStackTrace()
        }
    }
}