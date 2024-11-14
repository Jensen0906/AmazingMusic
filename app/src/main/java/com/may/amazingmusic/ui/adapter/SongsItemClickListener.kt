package com.may.amazingmusic.ui.adapter

import com.may.amazingmusic.bean.Song

/**
 *
 * @author May
 * @date 2024/9/16 15:09
 * @description SongsItemClickListener
 */
interface SongsItemClickListener {
    fun itemClickListener(song: Song)
    fun addSongToList(song: Song)
    fun showSongInfo(song: Song)
    fun favorite(song: Song, position: Int)
}