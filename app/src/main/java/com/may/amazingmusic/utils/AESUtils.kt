package com.may.amazingmusic.utils

import android.util.Base64
import com.may.amazingmusic.bean.User
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description AESUtils
 */

private val SECRET_KEY = "Jensen0906xxm423"
private val AES = "AES"
private val CHARSET = Charset.forName("utf-8")
private val CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding"

fun String?.AESEncode(): String {
    if (isNullOrEmpty()) {
        return ""
    }
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
    val byteArray = SECRET_KEY.toByteArray(CHARSET)
    val keySpec = SecretKeySpec(byteArray, AES)
    val iv = IvParameterSpec(byteArray)
    return try {
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        val encrypted = cipher.doFinal(toByteArray(CHARSET))
        Base64.encodeToString(encrypted, Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun String?.AESDecode(): String {
    if (isNullOrEmpty()) {
        return ""
    }
    val encrypted = Base64.decode(toByteArray(CHARSET), Base64.NO_WRAP)
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
    val byteArray = SECRET_KEY.toByteArray(CHARSET)
    val keySpec = SecretKeySpec(byteArray, AES)
    val iv = IvParameterSpec(byteArray)
    return try {
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val original = cipher.doFinal(encrypted)
        String(original, CHARSET)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun User.makePassowrdEncode(): User {
    val user = User()
    val data = this
    return user.apply {
        uid = data.uid
        username = data.username
        password = data.password.AESEncode()
        userStatus = data.userStatus
    }
}