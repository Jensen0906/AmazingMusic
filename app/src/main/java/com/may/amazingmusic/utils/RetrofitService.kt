package com.may.amazingmusic.utils

import com.may.amazingmusic.constant.NetWorkConst
import com.may.amazingmusic.utils.network.KuwoApi
import com.may.amazingmusic.utils.network.NetWorkApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description RetrofitService
 */

object RetrofitService {
    private val retrofit = Retrofit.Builder()
        .baseUrl(NetWorkConst.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    private val kuwoRetrofit = Retrofit.Builder()
        .baseUrl(NetWorkConst.KUWO_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getApi(): NetWorkApi {
        return retrofit.create()
    }

    fun getKuwoApi(): KuwoApi = kuwoRetrofit.create()
}