package com.may.amazingmusic.bean

/**
 * @Author Jensen
 * @Date 2025/3/9 11:30
 */
class KuwoSong {
    var rid: Long = 0
    var vid: String? = null
    var name: String? = null
    var artist: String? = null
    var pic: String? = null
    var lrc: String? = null
    var url: String? = null

    override fun toString(): String {
        return "KuwoSong(rid=$rid, vid=$vid, name=$name, artist=$artist, pic=$pic, lrc=$lrc, url=$url)"
    }
}