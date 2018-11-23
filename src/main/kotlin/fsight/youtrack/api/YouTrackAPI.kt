package fsight.youtrack.api

import com.google.gson.GsonBuilder
import fsight.youtrack.Converter
import fsight.youtrack.NEW_ROOT_REF
import fsight.youtrack.ROOT_REF
import fsight.youtrack.api.etl.bundles.CustomField
import fsight.youtrack.models.*

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface YouTrackAPI {
    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("issues?fields=idReadable,id")
    fun createIssue(
        @Header("Authorization") auth: String,
        @Body model: String
    ): Call<String>

    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("commands")
    fun postCommand(
        @Header("Authorization") auth: String,
        @Body model: String
    ): Call<String>

    @Headers("Accept: application/json")
    @GET("admin/customfield/bundle/{bundleName}")
    fun getBundleValues(
        @Header("Authorization") auth: String,
        @Path("bundleName") bundleName: String
    ): Call<EnumBundle>

    @Headers("Accept: application/json")
    @GET("admin/customFieldSettings/customFields?\$top=-1&fields=name,id,aliases,fieldType(id),instances(id,project(shortName,id),bundle(name,values(id,name),id))")
    fun getBundleValues(
        @Header("Authorization") auth: String
    ): Call<List<CustomField>>

    @Headers("Accept: application/json")
    @GET("issue")
    fun getIssueList(
        @Header("Authorization") auth: String,
        @Query("with") with: ArrayList<String>?,
        @Query("after") skip: Int,
        @Query("max") max: Int
    ): Call<Issues>

    @Headers("Accept: application/json")
    @GET("issue")
    fun getIssueList(
        @Header("Authorization") auth: String,
        @Query("filter") filter: String?,
        @Query("with") with: ArrayList<String>?,
        @Query("after") skip: Int,
        @Query("max") max: Int
    ): Call<Issues>

    @Headers("Accept: application/json")
    @GET("issue/{issueId}/changes")
    fun getIssueHistory(
        @Header("Authorization") auth: String,
        @Path("issueId") issueId: String
    ): Call<HistoricalChanges>

    @Headers("Accept: application/json")
    @GET("issue/{id}?with=projectShortName")
    fun check(
        @Header("Authorization") auth: String,
        @Path("id") issueId: String
    ): Call<Issue>

    @Headers("Accept: application/json")
    @GET("issue/{issueId}/timetracking/workitem")
    fun getWorkItems(
        @Header("Authorization") auth: String,
        @Path("issueId") issueId: String
    ): Call<List<WorkItem>>

    @Headers("Accept: application/json")
    @GET("project/all")
    fun getProjectsList(@Header("Authorization") auth: String): Call<List<ProjectModel>>

    @Headers("Accept: application/json")
    @GET("admin/project/{projectId}/customfield")
    fun getProjectCustomFields(
        @Header("Authorization") auth: String,
        @Path("projectId") projectId: String
    ): Call<List<ProjectCustomField>>

    @Headers("Accept: application/json")
    @GET("{path}")
    fun getProjectCustomFieldParameters(
        @Header("Authorization") auth: String,
        @Path("path", encoded = true) path: String
    ): Call<ProjectCustomFieldParameters>

    @Headers("Accept: application/json")
    @GET("admin/users?fields=id,name,email,ringId,login,fullName&\$top=-1")
    fun getUserDetails(
        @Header("Authorization") auth: String
    ): Call<List<UserDetails>>

    @Headers("Accept: application/json")
    @GET("admin/user")
    fun getUsers(
        @Header("Authorization") auth: String,
        @Query("start") start: Int
    ): Call<List<User>>

    companion object Factory {
        fun create(converter: Converter = Converter.SCALAR): YouTrackAPI {
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
                .create(YouTrackAPI::class.java)
        }

        fun createOld(converter: Converter): YouTrackAPI {
            val converterFactory = when (converter) {
                Converter.SCALAR -> {
                    ScalarsConverterFactory.create()
                }
                else -> {
                    val gson = GsonBuilder().setLenient().create()
                    GsonConverterFactory.create(gson)
                }
            }
            return Retrofit.Builder()
                .baseUrl(ROOT_REF)
                .addConverterFactory(converterFactory)
                .build()
                .create(YouTrackAPI::class.java)

        }
    }
}