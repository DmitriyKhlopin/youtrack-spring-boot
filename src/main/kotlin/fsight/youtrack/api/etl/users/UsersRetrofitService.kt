package fsight.youtrack.api.etl.users

import com.google.gson.GsonBuilder
import fsight.youtrack.ROOT_REF
import fsight.youtrack.models.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query


interface UsersRetrofitService {
    @Headers("Accept: application/json")
    @GET("admin/user")
    fun get(
            @Header("Authorization") auth: String,
            @Query("start") start: Int): Call<List<User>>

    companion object Factory {
        fun create(): UsersRetrofitService {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder().baseUrl(ROOT_REF).addConverterFactory(GsonConverterFactory.create(gson)).build()
            return retrofit.create(UsersRetrofitService::class.java)
        }
    }
}