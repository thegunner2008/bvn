package tamhoang.bvn.data.remote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("/api/v1/account/expire")
    fun getExpireDate(@Field("imei") imei: String, @Field("serial") serial: String): Call<ResponseModel?>?

    @Headers("Accept: application/json")
    @POST("ld")
    fun createPartner(@Body request: Map<String, String>): Call<Any?>

    @Headers("Content-Type: application/json")
    @POST("https://id.lotusapi.com/auth/sign-in")
    fun lotusSignIn(@Body requestBody: Map<String, String>): Call<Map<String, String>>

    @Headers("Content-Type: application/json")
    @GET("https://lotto.lotusapi.com/user-game-settings/player")
    fun lotusPlayer(@Header("Authorization") token: String): Call<ResponseBody>
}