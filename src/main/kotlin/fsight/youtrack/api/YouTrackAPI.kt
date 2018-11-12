package fsight.youtrack.api

import fsight.youtrack.NEW_ROOT_REF
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface YouTrackAPI {
    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("issues?fields=idReadable,id")
    fun createIssue(
            @Header("Authorization") auth: String,
            @Body model: String): Call<String>

    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("commands")
    fun postCommand(
            @Header("Authorization") auth: String,
            @Body model: String): Call<String>

    companion object Factory {
        fun create(): YouTrackAPI = Retrofit
                .Builder()
                .baseUrl(NEW_ROOT_REF)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(YouTrackAPI::class.java)
    }
}