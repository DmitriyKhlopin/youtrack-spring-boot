package fsight.youtrack.api.etl.issues

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.WorkItem
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface WorkItemRetrofitService {
    @Headers("Accept: application/json")
    @GET("issue/{issueId}/timetracking/workitem")
    fun getWorkItems(
            @Header("Authorization") auth: String,
            @Path("issueId") issueId: String
    ): Call<List<WorkItem>>

    companion object Factory {
        fun create(): WorkItemRetrofitService {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(WorkItemRetrofitService::class.java)
        }
    }
}