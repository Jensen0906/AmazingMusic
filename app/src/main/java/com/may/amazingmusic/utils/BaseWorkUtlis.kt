package com.may.amazingmusic.utils

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText

/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description BaseWorkUtlis
 */

private val asTextType: HideReturnsTransformationMethod =
    HideReturnsTransformationMethod.getInstance()
private val asPassType: PasswordTransformationMethod = PasswordTransformationMethod.getInstance()

/**
 * add a text change listener to make image show & not show
 *
 * image is mean password needshow or don't show
 * */
fun TextInputEditText.changeShowType(show: ImageView, notShow: ImageView) {
    addTextChangedListener {
        if (text?.toString().isNullOrEmpty()) {
            show.visibility = View.INVISIBLE
            notShow.visibility = View.INVISIBLE
        } else if (transformationMethod == asTextType) {
            show.visibility = View.VISIBLE
            notShow.visibility = View.INVISIBLE
        } else if (transformationMethod == asPassType) {
            show.visibility = View.INVISIBLE
            notShow.visibility = View.VISIBLE
        } else {
            show.visibility = View.INVISIBLE
            notShow.visibility = View.VISIBLE
        }
    }
}

fun ImageView.changePassShow(password: TextInputEditText, needShow: Boolean) {
    setOnClickListener {
        if (needShow) {
            password.run {
                transformationMethod = asTextType
                if (text != null) setSelection(text!!.length)
            }
        } else {
            password.run {
                transformationMethod = asPassType
                if (text != null) setSelection(text!!.length)
            }
        }
    }
}