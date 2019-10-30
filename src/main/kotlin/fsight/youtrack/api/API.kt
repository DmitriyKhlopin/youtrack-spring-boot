package fsight.youtrack.api

import fsight.youtrack.Converter
import fsight.youtrack.api.tfs.TFSData
import fsight.youtrack.getConverterFactory
import fsight.youtrack.getOkhttpClient
import fsight.youtrack.models.ServerStatus
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface API {
    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @GET("api/status")
    fun getStatus(): Call<ServerStatus>

    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("api/tfs/serviceHooks")
    fun postHook(
            @Body body: TFSData.Hook?
    ): Call<Any>

    companion object Factory {
        fun create(converter: Converter = Converter.SCALAR, environment: String): API {
            val url = when (environment) {
                "PROD" -> "http:/10.30.207.22:8080/"
                else -> "http:/10.9.172.76:8080/"
            }
            return Retrofit
                    .Builder()
                    .baseUrl(url)
                    .client(getOkhttpClient())
                    .addConverterFactory(getConverterFactory(converter))
                    .build()
                    .create(API::class.java)
        }
    }
}
