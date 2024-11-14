package com.may.amazingmusic.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.may.amazingmusic.App.Companion.appContext
import com.may.amazingmusic.R
import com.may.amazingmusic.bean.User
import com.may.amazingmusic.constant.NetWorkConst.CONTENT_TYPE
import com.may.amazingmusic.repository.UserRepository
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.makePassowrdEncode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * @Author Jensen
 * @Date 2023/10/07
 */
@SuppressLint("CheckResult")
class UserViewModel : ViewModel() {
    private val TAG = "UserViewModel";
    private var _user = MutableLiveData<User?>()
    val userLiveData: LiveData<User?> = _user

    private val repository = UserRepository()

    fun login(user: User?) {
        if (user == null || user.username.isNullOrEmpty() || user.password.isNullOrEmpty()) {
            ToastyUtils.warning(appContext.getString(R.string.username_or_password_empty))
            _user.postValue(null)
            return
        }

        val requestBody =
            Gson().toJson(user.makePassowrdEncode()).toRequestBody(CONTENT_TYPE.toMediaTypeOrNull())
        Log.d(TAG, "login: requestBody: $requestBody, user: $user, userEncode: ${user.makePassowrdEncode()}")
        viewModelScope.launch {
            repository.login(_user, requestBody)
        }
    }

    val registerUser = MutableLiveData<User?>()
    fun register(user: User?, pass2: String?) {
        if (user == null || user.username.isNullOrEmpty() || user.password.isNullOrEmpty()) {
            ToastyUtils.warning(appContext.getString(R.string.username_or_password_empty))
            registerUser.postValue(null)
        } else if (user.password != pass2) {
            ToastyUtils.warning(appContext.getString(R.string.twice_password_not_same))
            registerUser.postValue(null)
        } else {
            val requestBody =
                Gson().toJson(user.makePassowrdEncode()).toRequestBody(CONTENT_TYPE.toMediaTypeOrNull())
            viewModelScope.launch {
                val uid = repository.register(requestBody)
                if (uid > 0) {
                    user.uid = uid
                    registerUser.value = user.makePassowrdEncode()
                } else {
                    registerUser.postValue(null)
                }
            }
        }
    }

    val userId: MutableStateFlow<Int?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            userId.update { DataStoreManager.userIDFlow.first() }
        }
    }
}