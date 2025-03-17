package com.may.amazingmusic.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * @author May
 * @date 2025/3/17 14:17
 * @description SongList
 */
class SongList() : Parcelable {
    var rid: Long = -1
    var name: String? = null
    var total: Long = 0
    var pic: String? = null
    var list: String? = null

    constructor(parcel: Parcel) : this() {
        rid = parcel.readLong()
        name = parcel.readString()
        total = parcel.readLong()
        pic = parcel.readString()
        list = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(rid)
        parcel.writeString(name)
        parcel.writeLong(total)
        parcel.writeString(pic)
        parcel.writeString(list)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "SongList(rid=$rid, name=$name, total=$total, pic=$pic, list=$list)"
    }

    companion object CREATOR : Parcelable.Creator<SongList> {
        override fun createFromParcel(parcel: Parcel): SongList {
            return SongList(parcel)
        }

        override fun newArray(size: Int): Array<SongList?> {
            return arrayOfNulls(size)
        }
    }
}