package fsight.youtrack.etl.bundles.v2

import com.google.gson.GsonBuilder
import fsight.youtrack.NEW_ROOT_REF
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers


interface BundleRetrofitServiceV2 {
    @Headers("Accept: application/json")
    @GET("admin/customFieldSettings/customFields?\$top=-1&fields=name,id,aliases,fieldType(id),instances(id,project(shortName,id),bundle(name,values(id,name),id))")
    fun getBundleValues(
            @Header("Authorization") auth: String): Call<List<CustomField>>

    companion object Factory {
        fun create(): BundleRetrofitServiceV2 {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(NEW_ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(BundleRetrofitServiceV2::class.java)
        }
    }
}

data class BundleValue(val id: String?, val name: String?, var projectId: String? = null, var projectName: String? = null, var fieldId: String? = null, var fieldName: String? = null, var `$type`: String?)
data class BundleInstance(val values: ArrayList<BundleValue>?, val name: String?, val id: String?, val `$type`: String?)
data class InstanceProject(val shortName: String?, val id: String?, val `$type`: String?)
data class FieldInstance(val id: String?, val project: InstanceProject?, val bundle: BundleInstance?, val `$type`: String?)
data class CustomField(val name: String?, val id: String?, val aliases: String?, val instances: List<FieldInstance>?, val `$type`: String?)