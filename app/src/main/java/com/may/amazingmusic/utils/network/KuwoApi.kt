package com.may.amazingmusic.utils.network

import com.may.amazingmusic.bean.BannerData
import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.bean.Lrclist
import com.may.amazingmusic.bean.SongListInfo
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @Author Jensen
 * @Date 2025/3/9 11:25
 */
interface KuwoApi {

    @GET("/")
    suspend fun searchSongResult(
        @Query("name") keyword: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): ApiResult<List<KuwoSong>?>

    @GET("/")
    suspend fun getLrc(@Query("id") rid: Long, @Query("type") type: String): ApiResult<Lrclist?>

    @GET("banner/")
    suspend fun getBanner(): ApiResult<BannerData>

    @GET("/")
    suspend fun getSongListInfo(
        @Query("id") songListId: Long,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10,
        @Query("type") type: String = "list"
    ): ApiResult<SongListInfo>
}