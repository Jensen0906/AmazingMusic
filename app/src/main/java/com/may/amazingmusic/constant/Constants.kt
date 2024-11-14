package com.may.amazingmusic.constant

/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description NetWorkConst
 */
object NetWorkConst {
    const val BASE_URL = "http://110.40.128.208:42300/"
    const val SUCCESS_STATUS = 200
    const val CONTENT_TYPE = "application/json;"

    const val FUN_VIDEO_URL = "https://amazingmusic-1321711729.cos.ap-shanghai.myqcloud.com/video/fun_video.mp4"
}

object BaseWorkConst {
    const val TO_REGISTER_FOR_RESULT = 423
    const val REGISTER_SUCCESS_NAME = "register_success_name"
    const val ADD_LIST_AND_PLAY = -2
    const val ADD_LIST_NEXT = -3
    const val ADD_LIST_LAST = -4

    const val REPEAT_MODE_SINGLE = -3
    const val REPEAT_MODE_LOOP = -2
    const val REPEAT_MODE_SHUFFLE = -1
}

object IntentConst {
    const val PLAY_NOW_ACTION = "play_now_action"
    const val SONG_URL = "song_url"
    const val PENDING_INTENT_ACTION = "pending_intent_action"
    const val REGISTER_SUCCESS_ACTION = "register_success_action"
    const val REGISTER_SUCCESS_INTENT_EXTRA = "register_success_intent_extra"
}
