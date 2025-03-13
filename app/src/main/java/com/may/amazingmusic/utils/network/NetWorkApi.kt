package com.may.amazingmusic.utils.network

import com.may.amazingmusic.bean.KuwoSong
import com.may.amazingmusic.bean.Song
import com.may.amazingmusic.bean.User
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @Author May
 * @Date 2024/09/05 16:25
 * @Description NetWorkApi
 */

interface NetWorkApi {

    @POST("user/login")
    suspend fun login(@Body body: RequestBody): ApiResult<User?>

    @POST("user/register")
    suspend fun register(@Body body: RequestBody): ApiResult<Int?>

    @POST("song/getsongs")
    suspend fun getSongs(@Body body: RequestBody): ApiResult<List<Song>?>

    @POST("song/find-songs-by-any")
    suspend fun findSongsByAny(@Query("keyword") keyword: String): ApiResult<List<Song>?>

    @POST("favorite/getsongs")
    suspend fun getFavoriteSongs(@Body body: RequestBody): ApiResult<List<Song>?>

    @POST("favorite/getsongsids")
    suspend fun getFavoriteSongsIds(@Body body: RequestBody): ApiResult<List<Long>?>

    @POST("favorite/operatefavorite")
    suspend fun operateFavorite(@Body body: RequestBody): ApiResult<Int?>

    @POST("feedback/addfeedback")
    suspend fun addFeedback(@Body body: RequestBody): ApiResult<Int?>

    @POST("kuwo/get-kuwosongs")
    suspend fun getFavoriteKuwoSongs(@Body body: RequestBody): ApiResult<List<KuwoSong>?>

    @POST("kuwo/operate-kuwosong")
    suspend fun operateFavoriteKuwoSong(@Body body: RequestBody): ApiResult<Int?>

    @POST("kuwo/get-kuwosongrids")
    suspend fun getKuwoSongRids(@Body body: RequestBody): ApiResult<List<Long>?>
}