package fsight.youtrack.issues

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.HistoricalChanges
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path


interface HistoryRetrofitService {
    @Headers("Accept: application/json")
    @GET("issue/{issueId}/changes")
    fun getIssueHistory(@Header("Authorization") auth: String, @Path("issueId") issueId: String): Call<HistoricalChanges>

    companion object Factory {
        fun create(): HistoryRetrofitService {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(HistoryRetrofitService::class.java)
        }
    }
}