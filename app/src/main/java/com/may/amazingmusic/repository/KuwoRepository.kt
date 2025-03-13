package com.may.amazingmusic.repository

import com.may.amazingmusic.bean.KuwoSong
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
            val result = baseApi.getFavoriteKuwoSongs(requestBody)
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
}