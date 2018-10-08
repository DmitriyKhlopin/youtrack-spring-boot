package fsight.youtrack.etl.projects

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.ProjectModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface ProjectsRetrofitService {
    @Headers("Accept: application/json")
    @GET("project/all")
    fun getProjectsList(@Header("Authorization") auth: String): Call<List<ProjectModel>>

    companion object Factory {
        fun create(): ProjectsRetrofitService {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(ProjectsRetrofitService::class.java)
        }
    }
}