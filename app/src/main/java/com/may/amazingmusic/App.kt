package com.may.amazingmusic

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description App
 */
class App : Application() {

    companion object {
        lateinit var appContext: Context
        val mainScope = CoroutineScope(Dispatchers.Main)
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

    }
}

val Context.userDataStore by preferencesDataStore(name = "user_info")