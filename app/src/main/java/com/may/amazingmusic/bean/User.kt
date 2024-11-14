package com.may.amazingmusic.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.may.amazingmusic.BR

/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description User
 */
class User : BaseObservable() {
    var uid = 0

    @get: Bindable
    var username: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.username)
        }

    @get: Bindable
    var password: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.password)
        }
    var userStatus = 0
    var errorMsg = ""

    override fun toString(): String {
        return "User(uid=$uid, username=$username, password=$password, userStatus=$userStatus)"
    }
}
