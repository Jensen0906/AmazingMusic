package com.may.amazingmusic.bean

import com.google.gson.annotations.SerializedName

/**
 * @Author Jensen
 * @Date 2025/3/9 11:30
 */
class KuwoSong {
    @SerializedName("n") var index = 0
    var songname: String? = null
    var singer: String? = null
    @SerializedName("song_rid") var songRid: String? = null
    var cover: String? = null
    var link: String? = null
    var url: String? = null

    override fun toString(): String {
        return "KuwoSong(index=$index, songname=$songname, singer=$singer, songRid=$songRid, cover=$cover, link=$link, url=$url)"
    }
}