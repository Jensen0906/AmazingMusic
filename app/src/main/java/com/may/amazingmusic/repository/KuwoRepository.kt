package com.may.amazingmusic.repository

import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.utils.RetrofitService
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * @Author Jensen
 * @Date 2025/3/9 11:36
 */
class KuwoRepository {
    private val TAG = this.javaClass.simpleName

    private val api = RetrofitService.getKuwoApi()

    suspend fun searchSongs(songs: MutableSharedFlow<List<KuwoSong>?>, keyword: String, page: Int, limit: Int) {
        runCatching {
            val result = api.searchSongResult(keyword, page, limit)
            songs.tryEmit(result.data)
        }.onFailure {
            it.printStackTrace()
            songs.tryEmit(null)
        }
    }
}