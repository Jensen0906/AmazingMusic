package com.may.amazingmusic.ui.adapter

/**
 *
 * @author May
 * @date 2024/9/17 1:37
 * @description PlaylistItemClickListener
 */
interface PlaylistItemClickListener {
    fun itemClickListener(position: Int)
    fun itemRemoveListener(position: Int)
}