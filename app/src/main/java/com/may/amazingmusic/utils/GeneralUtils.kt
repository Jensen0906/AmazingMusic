package com.may.amazingmusic.utils

/**
 *
 * @author May
 * @date 2024/9/15 21:22
 * @description GeneralUtils
 */
fun Boolean?.isTrue(): Boolean = this ?: false

fun Boolean?.isFalse(): Boolean = if (this == null) false else !this

fun Int?.orZero(): Int = this ?: 0

fun Int?.orInvalid(): Int = this ?: -1