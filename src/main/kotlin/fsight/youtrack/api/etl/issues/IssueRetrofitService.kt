package fsight.youtrack.api.etl.issues

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.Issues
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query


interface IssueRetrofitService {
    @Headers("Accept: application/json")
    @GET("issue")
    fun getIssueList(
            @Header("Authorization") auth: String,
            @Query("filter") filter: String?,
            @Query("with") with: ArrayList<String>?,
            @Query("after") skip: Int,
            @Query("max") max: Int): Call<Issues>

    companion object Factory {
        fun create(): IssueRetrofitService {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(IssueRetrofitService::class.java)
        }
    }
}