package fsight.youtrack.api.etl.users

import com.google.gson.GsonBuilder
import fsight.youtrack.NEW_ROOT_REF
import fsight.youtrack.models.UserDetails
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers


interface UserDetailsRetrofitInterface {
    @Headers("Accept: application/json")
    @GET("admin/users?fields=id,name,email,ringId,login,fullName&\$top=-1")
    fun get(
            @Header("Authorization") auth: String): Call<List<UserDetails>>

    companion object Factory {
        fun create(): UserDetailsRetrofitInterface {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(NEW_ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(UserDetailsRetrofitInterface::class.java)
        }
    }
}