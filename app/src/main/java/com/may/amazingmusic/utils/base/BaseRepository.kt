package com.may.amazingmusic.utils.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.may.amazingmusic.constant.NetWorkConst.SUCCESS_STATUS
import com.may.amazingmusic.utils.RetrofitService
import com.may.amazingmusic.utils.ToastyUtils
import com.may.amazingmusic.utils.network.ApiResult
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.IOException

/**
 * @Author Jensen
 * @Date 2023/10/08
 */

abstract class BaseRepository {

    protected val api = RetrofitService.getApi()
    suspend fun <T> execute(
        block: suspend () -> ApiResult<T>,
        response: MutableLiveData<T>,
    ) {
        try {
            val result = block.invoke()
            Log.d("TAG", "execute: result.code=${result.code}, result.data=${result.data}")
            response.postValue(result.data)
        } catch (e: IOException) {
            e.printStackTrace()
            response.postValue(null)
        }
    }

    suspend fun <T> execute(
        block: suspend () -> ApiResult<T>,
        response: MutableSharedFlow<T?>,
    ) {
        try {
            val result = block.invoke()
            Log.d("TAG", "execute: result.code=${result.code}, result.data=${result.data}")
            response.tryEmit(result.data)
        } catch (e: IOException) {
            e.printStackTrace()
            response.tryEmit(null)
        }
    }

    suspend fun <T> executeResult(block: suspend () -> ApiResult<T?>, shouldToast: Boolean = true): Boolean {
        try {
            val result = block.invoke()
            Log.d("TAG", "execute: result.code=${result.code}, result.data=${result.data}")
            if (result.code != SUCCESS_STATUS) {
                result.msg?.let { if (shouldToast) ToastyUtils.error(it) }
            }
            return result.code == SUCCESS_STATUS
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun <T> executeData(block: suspend () -> ApiResult<T?>, shouldToast: Boolean = true): Int {
        var data = -1
        try {
            val result = block.invoke()
            Log.d("TAG", "execute: result.code=${result.code}, result.data=${result.data}")
            if (result.code != SUCCESS_STATUS) {
                result.msg?.let { if (shouldToast) ToastyUtils.error(it) }
            } else data = result.data as Int
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return data
    }
}