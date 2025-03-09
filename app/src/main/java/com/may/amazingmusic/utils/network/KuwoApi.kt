package com.may.amazingmusic.utils.network

import com.may.amazingmusic.bean.KuwoSong
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @Author Jensen
 * @Date 2025/3/9 11:25
 */
interface KuwoApi {

    @GET("KoWo_Dg.php")
    suspend fun searchSongResult(@Query("type") type: String, @Query("msg") keyword: String) : ApiResult<List<KuwoSong>?>

    @GET("KoWo_Dg.php")
    suspend fun selectSongResult(@Query("type") type: String, @Query("msg") keyword: String, @Query("n") index: Int) : ApiResult<KuwoSong?>
}