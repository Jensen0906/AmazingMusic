package com.may.amazingmusic.utils.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.constant.BaseWorkConst.REPEAT_MODE_LOOP
import com.may.amazingmusic.constant.BaseWorkConst.REPEAT_MODE_SHUFFLE
import com.may.amazingmusic.constant.BaseWorkConst.REPEAT_MODE_SINGLE
import com.may.amazingmusic.constant.NetWorkConst.FUN_VIDEO_URL
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.orZero
import okhttp3.OkHttpClient
import java.io.File

/**
 *
 * @author May
 * @date 2024/9/16 0:35
 * @description PlayerManager
 */
@OptIn(UnstableApi::class)
object PlayerManager {
    private val TAG = this.javaClass.simpleName

    var player: ExoPlayer? = null
    var playerListener: PlayerListener? = null
    var playingSongUrl: String? = null
    var page = 1
    val curSongIndexLiveData = MutableLiveData(-1)
    val repeatModeLiveData = MutableLiveData(ExoPlayer.REPEAT_MODE_OFF)

    val playlist: MutableList<Song> = mutableListOf()

    fun setPlayerListener() {
        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (player?.currentMediaItem != funVideoMediaItem && playlist.isNotEmpty()) {
                    playerListener?.onIsPlayingChanged(
                        isPlaying,
                        playlist[player?.currentMediaItemIndex.orZero()].title
                    )
                }
                super.onIsPlayingChanged(isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                playingSongUrl = player?.currentMediaItem?.localConfiguration?.uri?.toString()
                if (player?.currentMediaItem != funVideoMediaItem) {
                    curSongIndexLiveData.postValue(player?.currentMediaItemIndex ?: -1)
                }
                super.onMediaItemTransition(mediaItem, reason)
            }
        })
    }

    fun playNextSong() {
        if (repeatModeLiveData.value == REPEAT_MODE_SINGLE) player?.seekTo(0)
        else player?.seekToNextMediaItem()
        player?.play()
    }

    fun playPreviousSong() {
        if (repeatModeLiveData.value == REPEAT_MODE_SINGLE) player?.seekTo(0)
        else player?.seekToPreviousMediaItem()
        player?.play()
    }

    fun playSongByPosition(position: Int) {
        val currentIndex = player?.currentMediaItemIndex
        if (currentIndex != position) {
            curSongIndexLiveData.postValue(currentIndex ?: -1)
            player?.seekTo(position, 0)
        }
        player?.play()
    }

    fun buildCacheDataSourceFactory(context: Context): DataSource.Factory {
        val okHttpClient = OkHttpClient() // 用于网络数据请求
        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)

        // 包装缓存的数据源工厂
        return CacheDataSource.Factory()
            .setCache(getCache(context))
            .setUpstreamDataSourceFactory(okHttpDataSourceFactory) // 上游网络数据源
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR) // 出错时忽略缓存
    }

    fun changePlayMode() {
        Log.d(TAG, "changePlayMode: repeatMode=${repeatModeLiveData.value}")
        when (repeatModeLiveData.value) {
            REPEAT_MODE_SINGLE -> {
                // Change to REPEAT_MODE_LOOP (REPEAT_MODE_ALL, close shuffle mode)
                player?.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                player?.shuffleModeEnabled = false
                repeatModeLiveData.postValue(REPEAT_MODE_LOOP)
            }

            REPEAT_MODE_LOOP -> {
                // Change to REPEAT_MODE_SHUFFLE (REPEAT_MODE_ALL, open shuffle mode)
                player?.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                player?.shuffleModeEnabled = true
                repeatModeLiveData.postValue(REPEAT_MODE_SHUFFLE)
            }

            REPEAT_MODE_SHUFFLE -> {
                // Change to REPEAT_MODE_SINGLE (REPEAT_MODE_ONE, close shuffle mode)
                player?.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                player?.shuffleModeEnabled = false
                repeatModeLiveData.postValue(REPEAT_MODE_SINGLE)
            }

            else -> { // Default is 0 (ExoPlayer.REPEAT_MODE_OFF). Actually it should be REPEAT_MODE_LOOP
                // Change to REPEAT_MODE_SHUFFLE (REPEAT_MODE_ALL, open shuffle mode)
                player?.repeatMode = ExoPlayer.REPEAT_MODE_ALL
                player?.shuffleModeEnabled = true
                repeatModeLiveData.postValue(REPEAT_MODE_SHUFFLE)
            }
        }
    }

    fun playAsRepeatMode() {
        if (player?.shuffleModeEnabled.isTrue()) playNextSong()
        else player?.play()
    }

    fun removeMediaItem(position: Int) {
        val index = player?.currentMediaItemIndex
        val count = playlist.size
        if (position >= count) return
        if (position == index) {
            if (count > 1) {// 不止一首歌
                if (count == position + 1) { // 不止一首歌，且正在播放(并且remove)最后一首, 则播放列表第一首歌
                    playSongByPosition(0)
                } else { // 不止一首歌，且播放(remove)的不是最后一首，直接播放下一首歌
                    playSongByPosition(position + 1)
                }
            }
        }
        player?.removeMediaItem(position)
        playlist.removeAt(position)
    }

    fun clearPlaylist() {
        player?.clearMediaItems()
        playlist.clear()
        curSongIndexLiveData.postValue(-1)
    }

    private var simpleCache: SimpleCache? = null
    private fun getCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "music_cache")
            val cacheEvictor = LeastRecentlyUsedCacheEvictor(200L * 1024 * 1024) // 100MB 缓存
            val databaseProvider = StandaloneDatabaseProvider(context)

            simpleCache = SimpleCache(cacheDir, cacheEvictor, databaseProvider)
        }
        return simpleCache!!
    }

    // for test
    fun addAnalyticsListenerForTest() {
        player?.addAnalyticsListener(object : AnalyticsListener {
            override fun onBandwidthEstimate(
                eventTime: AnalyticsListener.EventTime,
                totalLoadTimeMs: Int,
                totalBytesLoaded: Long,
                bitrateEstimate: Long
            ) {
                Log.d(TAG, "TotalBytesLoaded: $totalBytesLoaded, Bitrate: $bitrateEstimate")
                super.onBandwidthEstimate(eventTime, totalLoadTimeMs, totalBytesLoaded, bitrateEstimate)
            }
        })
    }

    fun release() {
        clearPlaylist()
        player?.release()
        player = null
    }

    private val funVideoMediaItem = MediaItem.fromUri(FUN_VIDEO_URL)
    fun playFunVideo() {
        clearPlaylist()
        player?.setMediaItem(funVideoMediaItem)
        player?.play()
    }
}