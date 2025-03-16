package com.may.amazingmusic.bean

/**
 * @Author Jensen
 * @Date 2025/3/16 14:26
 */
class SongListInfo {
    var id: Long = -1
    var img: String? = null
    var name: String? = null
    var tag: String? = null
    var musicList: List<KuwoSong>? = null

    override fun toString(): String {
        return "SongListInfo(id=$id, img=$img, name=$name, tag=$tag, musicList=$musicList)"
    }
}