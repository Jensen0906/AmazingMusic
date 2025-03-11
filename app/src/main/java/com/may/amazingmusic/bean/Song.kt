package com.may.amazingmusic.bean

import android.os.Parcel
import android.os.Parcelable

/**
 *
 * @author May
 * @date 2024/9/16 12:36
 * @description Song
 */
class Song() : Parcelable {
    var sid: Long = 0

    var title: String? = null

    var singer: String? = null

    var album: String? = null

    var url: String? = null

    var isFavorite = false

    var coverUrl : String? = null

    var lrc : String? = null



    constructor(parcel: Parcel) : this() {
        sid = parcel.readLong()
        title = parcel.readString()
        singer = parcel.readString()
        album = parcel.readString()
        url = parcel.readString()
        isFavorite = parcel.readByte() != 0.toByte()
        coverUrl = parcel.readString()
        lrc = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(sid)
        parcel.writeString(title)
        parcel.writeString(singer)
        parcel.writeString(album)
        parcel.writeString(url)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeString(coverUrl)
        parcel.writeString(lrc)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Song(sid=$sid, title=$title, singer=$singer, album=$album, url=$url, isFavorite=$isFavorite, coverUrl=$coverUrl, lrc=$lrc)"
    }


    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}