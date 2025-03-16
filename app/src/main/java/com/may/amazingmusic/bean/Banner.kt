package com.may.amazingmusic.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * @Author Jensen
 * @Date 2025/3/15 3:36
 */
class Banner() : Parcelable{
    var id: Long = 0L
    var pic: String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        pic = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(pic)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Banner(id=$id, pic=$pic)"
    }

    companion object CREATOR : Parcelable.Creator<Banner> {
        override fun createFromParcel(parcel: Parcel): Banner {
            return Banner(parcel)
        }

        override fun newArray(size: Int): Array<Banner?> {
            return arrayOfNulls(size)
        }
    }
}
