package fsight.youtrack.issues

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.Issue
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface IssueExistenceRetrofitService {
    @Headers("Accept: application/json")
    @GET("issue/{id}?with=projectShortName")
    fun check(
            @Header("Authorization") auth: String,
            @Path("id") issueId: String): Call<Issue>

    companion object Factory {
        fun create(): IssueExistenceRetrofitService {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(IssueExistenceRetrofitService::class.java)
        }
    }
}