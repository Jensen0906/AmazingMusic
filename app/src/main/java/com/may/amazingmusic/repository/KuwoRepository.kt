package com.may.amazingmusic.repository

import android.util.Log
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

    suspend fun searchSongs(songs: MutableSharedFlow<List<KuwoSong>?>, keyword: String) {
        runCatching {
            val result = api.searchSongResult("json", keyword)

            val kuwoSongs = ArrayList<KuwoSong>()
            result.data?.forEach {
                it.cover = getSongCover(keyword, it.index)
                kuwoSongs.add(it)
                songs.tryEmit(kuwoSongs)
            }
            Log.d(TAG, "searchSongs: result.code=${result.code}, result.data=${result.data}")
        }.onFailure {
            it.printStackTrace()
            songs.tryEmit(null)
        }
    }

    suspend fun selectSong(song: MutableSharedFlow<KuwoSong?>, keyword: String, index: Int, rid: String) {
        runCatching {
            val result = api.selectSongResult("json", keyword, index)
            Log.d(TAG, "selectSong: result.code=${result.code}, result.data=${result.data}")
            song.tryEmit(result.data?.also {
                it.songRid = rid
                it.index = index
            })
        }.onFailure {
            it.printStackTrace()
            song.tryEmit(null)
        }
    }

    private suspend fun getSongCover(keyword: String, index: Int) : String {
        runCatching {
            val result = api.selectSongResult("json", keyword, index)
            Log.d(TAG, "getSongCover: result.code=${result.code}, result.data=${result.data}")
            return  result.data?.cover ?: ""
        }.onFailure {
            it.printStackTrace()
        }
        return ""
    }
}