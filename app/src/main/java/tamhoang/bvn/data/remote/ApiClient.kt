package tamhoang.bvn.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    var baseUrl = ""

    var service: ApiService? = null
    fun init(baseUrl: String): ApiService {
        this.baseUrl = baseUrl
        val httpClient = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://${baseUrl.replace("=",".")}")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    @JvmStatic
    fun service(): ApiService {
        if (service == null) {
            service = init(baseUrl)
        }
        return service!!
    }
}