package com.may.amazingmusic.utils.network

/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description ApiResult
 */

class ApiResult<T>(
    var code: Int = -1,
    var msg: String? = null,
    var data: T? = null
)
