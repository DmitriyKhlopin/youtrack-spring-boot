package fsight.youtrack.etlV2.issues

import com.google.gson.GsonBuilder
import fsight.youtrack.AUTH
import fsight.youtrack.NEW_ROOT_REF
import fsight.youtrack.models.v2.Issue
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface IssuesFromYT {
    @Headers("Accept: application/json", "Authorization: $AUTH")
    @GET("issues")
    fun getIssueList(
        @Query("filter") filter: String?,
        @Query("after") skip: Int,
        @Query("max") max: Int
    ): Call<List<Issue>>

    @Headers("Accept: application/json", "Authorization: $AUTH")
    @GET("issues/{id}?fields=id,idReadable,project(id,name,shortName),reporter(login,name),updater(login,name),updated,resolved,summary,description,fields(projectCustomField(field(id,name)),value(id,name)),comments(author(login,name),text,created,updated)")
    fun getById(@Path("id") id: String): Call<Issue>

    companion object Factory {
        fun create(): IssuesFromYT {
            val gson = GsonBuilder().setLenient().create()
            val retrofit =
                Retrofit.Builder().baseUrl(NEW_ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(IssuesFromYT::class.java)
        }
    }
}
