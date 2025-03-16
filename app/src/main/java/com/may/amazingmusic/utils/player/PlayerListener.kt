package com.may.amazingmusic.utils.player

import androidx.media3.common.MediaItem

/**
 *
 * @author May
 * @date 2024/9/16 21:11
 * @description PlayerListener
 */
interface PlayerListener {
    fun onIsPlayingChanged(isPlaying: Boolean, title: String?) {}
    fun onMediaItemTransition(mediaItem: MediaItem?, position: Int) {}
}