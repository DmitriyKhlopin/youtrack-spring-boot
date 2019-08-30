package fsight.youtrack.api

import com.google.gson.GsonBuilder
import fsight.youtrack.Converter
import fsight.youtrack.models.ServerStatus
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface API {
    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("api/status")
    fun getStatus(): Call<ServerStatus>

    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("api/tfs/serviceHooks")
    fun postHook(
        @Body body: String?
    ): Call<Any>

    companion object Factory {
        fun create(converter: Converter = Converter.SCALAR, environment: String): API {
            val converterFactory = when (converter) {
                Converter.SCALAR -> {
                    ScalarsConverterFactory.create()
                }
                else -> {
                    val gson = GsonBuilder().setLenient().create()
                    GsonConverterFactory.create(gson)
                }
            }
            val url = if (environment == "PROD") "http:/10.30.207.22:8080/" else "http:/10.9.172.76:8080/"
            return Retrofit
                .Builder()
                .baseUrl(url)
                .addConverterFactory(converterFactory)
                .build()
                .create(API::class.java)
        }
    }
}
