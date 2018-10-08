package fsight.youtrack.etl.users

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.UserDetails
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path


interface UserDetailsInterface {
    @Headers("Accept: application/json")
    @GET("admin/user/{userId}")
    fun get(
            @Header("Authorization") auth: String,
            @Path("userId") userId: String): Call<UserDetails>

    companion object Factory {
        fun create(): UserDetailsInterface {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(UserDetailsInterface::class.java)
        }
    }
}