package com.may.amazingmusic.repository

import com.may.amazingmusic.utils.base.BaseRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.RequestBody

/**
 *
 * @author May
 * @date 2024/10/20 17:58
 * @description FeedbackRepository
 */
class FeedbackRepository : BaseRepository() {
    suspend fun addFeedback(addFeedbackResult: MutableSharedFlow<Int?>, requestBody: RequestBody) {
        execute({ api.addFeedback(requestBody) }, addFeedbackResult)
    }
}