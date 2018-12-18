package fsight.youtrack.api

import com.google.gson.GsonBuilder
import fsight.youtrack.Converter
import fsight.youtrack.NEW_ROOT_REF
import fsight.youtrack.models.YouTrackIssue
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface YouTrackAPIv2 {
    @Headers("Accept: application/json")
    @GET("issues")
    fun getIssueList(
        @Header("Authorization") auth: String,
        @Query("fields") fields: String,
        @Query("\$top") top: Int,
        @Query("\$skip") skip: Int
    ): Call<List<YouTrackIssue>>


    @Headers("Accept: application/json")
    @GET("issues")
    fun getIssueList(
        @Header("Authorization") auth: String,
        @Query("fields") fields: String,
        @Query("\$top") top: Int,
        @Query("\$skip") skip: Int,
        @Query("query") query: String
    ): Call<List<YouTrackIssue>>


    companion object Factory {
        fun create(converter: Converter = Converter.SCALAR): YouTrackAPIv2 {
            val converterFactory = when (converter) {
                Converter.SCALAR -> {
                    ScalarsConverterFactory.create()
                }
                else -> {
                    val gson = GsonBuilder().setLenient().create()
                    GsonConverterFactory.create(gson)
                }
            }
            return Retrofit
                .Builder()
                .baseUrl(NEW_ROOT_REF)
                .addConverterFactory(converterFactory)
                .build()
                .create(YouTrackAPIv2::class.java)
        }
    }
}
