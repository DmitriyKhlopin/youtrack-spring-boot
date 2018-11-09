package fsight.youtrack.api.etl.projects

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.ProjectCustomFieldParameters
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface ProjectCustomFieldParametersRetrofitService {
    @Headers("Accept: application/json")
    @GET("{path}")
    fun get(@Header("Authorization") auth: String,
            @Path("path", encoded = true) path: String): Call<ProjectCustomFieldParameters>

    companion object Factory {
        fun create(): ProjectCustomFieldParametersRetrofitService {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(ProjectCustomFieldParametersRetrofitService::class.java)
        }
    }
}