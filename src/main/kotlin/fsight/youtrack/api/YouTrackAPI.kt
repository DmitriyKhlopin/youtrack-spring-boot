package fsight.youtrack.api

import com.google.gson.GsonBuilder
import fsight.youtrack.Converter
import fsight.youtrack.NEW_ROOT_REF
import fsight.youtrack.ROOT_REF
import fsight.youtrack.etl.bundles.CustomField
import fsight.youtrack.models.EnumBundle
import fsight.youtrack.models.Issue
import fsight.youtrack.models.ProjectCustomField
import fsight.youtrack.models.ProjectCustomFieldParameters
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface YouTrackAPI {
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
    @GET("issue/{id}?with=projectShortName")
    fun check(
        @Header("Authorization") auth: String,
        @Path("id") issueId: String
    ): Call<Issue>

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
