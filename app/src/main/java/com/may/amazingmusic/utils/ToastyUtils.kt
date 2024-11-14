package com.may.amazingmusic.utils

import android.widget.Toast
import com.may.amazingmusic.App.Companion.appContext
import es.dmoral.toasty.Toasty

/**
 *
 * @author May
 * @date 2024/9/17 2:48
 * @description ToastyUtils
 */
object ToastyUtils {
    private var currentToast: Toast? = null

    fun success(msg: String) {
        currentToast?.cancel()
        currentToast = Toasty.success(appContext, msg)
        currentToast?.show()
    }

    fun info(msg: String) {
        currentToast?.cancel()
        currentToast = Toasty.info(appContext, msg)
        currentToast?.show()
    }

    fun warning(msg: String) {
        currentToast?.cancel()
        currentToast = Toasty.warning(appContext, msg)
        currentToast?.show()
    }

    fun error(msg: String) {
        currentToast?.cancel()
        currentToast = Toasty.error(appContext, msg)
        currentToast?.show()
    }
}