package com.may.amazingmusic.repository

import androidx.lifecycle.MutableLiveData
import com.may.amazingmusic.bean.User
import com.may.amazingmusic.utils.base.BaseRepository
import okhttp3.RequestBody


/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description UserRepository
 */
class UserRepository : BaseRepository() {

    suspend fun login(user: MutableLiveData<User?>, requestBody: RequestBody) {
        execute({ api.login(requestBody) }, user)
    }

    suspend fun register(requestBody: RequestBody): Int {
        return executeData({ api.register(requestBody) })
    }
}