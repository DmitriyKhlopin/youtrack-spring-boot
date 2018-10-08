package fsight.youtrack.etl.bundles

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.EnumBundle
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path


interface BundleRetrofitService {
    @Headers("Accept: application/json")
    @GET("admin/customfield/bundle/{bundleName}")
    fun getBundleValues(
            @Header("Authorization") auth: String,
            @Path("bundleName") bundleName: String): Call<EnumBundle>

    companion object Factory {
        fun create(): BundleRetrofitService {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(BundleRetrofitService::class.java)
        }
    }
}