package fsight.youtrack.api.etl.projects

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.ProjectCustomField
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface ProjectCustomFieldsRetrofitService {
    @Headers("Accept: application/json")
    @GET("admin/project/{projectId}/customfield")
    fun get(@Header("Authorization") auth: String,
            @Path("projectId") projectId: String): Call<List<ProjectCustomField>>

    companion object Factory {
        fun create(): ProjectCustomFieldsRetrofitService {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(ProjectCustomFieldsRetrofitService::class.java)
        }
    }
}