package com.may.amazingmusic.utils

import android.content.Context
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
fun Long?.orZero(): Long = this ?: 0

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
fun Long?.msToTimeString(): String {
    val s = orZero() / 1000
    return if (s <= 0) {
        "00:00"
    } else if (s in 1..59) {
        if (s % 60 < 10) "00:"+"0"+s % 60
        else "00:"+s % 60
    } else if (s in 60..599) {
        if (s % 60 < 10) "0"+s/60+":0"+s % 60
        else "0"+s/60+":"+s % 60
    } else {
        "00:00"
    }
}

fun Float.spToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}