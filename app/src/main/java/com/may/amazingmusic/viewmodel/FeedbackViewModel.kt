package com.may.amazingmusic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.may.amazingmusic.bean.Feedback
import com.may.amazingmusic.constant.NetWorkConst.CONTENT_TYPE
import com.may.amazingmusic.repository.FeedbackRepository
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.orZero
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

/**
 *
 * @author May
 * @date 2024/10/20 17:53
 * @description FeedbackViewModel
 */
class FeedbackViewModel : ViewModel() {
    private val TAG = this.javaClass.simpleName

    private val repository = FeedbackRepository()

    val addFeedbackResult = MutableSharedFlow<Int?>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun addFeedback(info: String?) {
        if (info.isNullOrEmpty()) {
            addFeedbackResult.tryEmit(0)
            return
        }
        viewModelScope.launch {
            val uid = DataStoreManager.userIDFlow.first().orZero()
            val requestBody = Gson().toJson(Feedback(uid, info, 1)).toRequestBody(CONTENT_TYPE.toMediaTypeOrNull())
            repository.addFeedback(addFeedbackResult, requestBody)
        }
    }
}