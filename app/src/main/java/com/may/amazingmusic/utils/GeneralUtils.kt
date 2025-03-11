package com.may.amazingmusic.utils

import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.bean.Song

/**
 *
 * @author May
 * @date 2024/9/15 21:22
 * @description GeneralUtils
 */
fun Boolean?.isTrue(): Boolean = this ?: false

fun Boolean?.isFalse(): Boolean = if (this == null) false else !this

fun Int?.orZero(): Int = this ?: 0

fun Int?.moreThanOne(): Boolean = if (this == null) false else this > 1

fun Int?.orInvalid(): Int = this ?: -1

fun KuwoSong?.convertToSong(): Song {
    return Song().also {
        this?.run {
            it.sid = rid
            it.title = name
            it.singer = artist
            it.url = url
            it.coverUrl = pic
            it.lrc = lrc
        }
    }
}