package com.simats.dermacareai.network

import com.simats.dermacareai.network.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("signup")
    suspend fun signUp(@Body user: Map<String, String>): Response<UserResponse>

    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @GET("users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): Response<UserResponse>

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadAvatar(
        @Header("Authorization") token: String,
        @Part avatar: MultipartBody.Part
    ): Response<UserResponse>

    @Multipart
    @POST("analyze")
    suspend fun analyzeSkin(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<AnalysisResponse>

    @GET("history")
    suspend fun getHistory(@Header("Authorization") token: String): Response<List<AnalysisResponse>>

    @GET("analysis/{analysis_id}")
    suspend fun getAnalysisDetails(
        @Header("Authorization") token: String,
        @Path("analysis_id") analysisId: Int
    ): Response<AnalysisResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): Response<Map<String, String>>

    @POST("verify-code")
    suspend fun verifyCode(@Body request: Map<String, String>): Response<Map<String, String>>

    @POST("new-password")
    suspend fun updatePassword(@Body request: Map<String, String>): Response<Map<String, String>>

    @GET("doctors")
    suspend fun getDoctors(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null
    ): Response<List<com.simats.dermacareai.models.Doctor>>

    @POST("update")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Body user: Map<String, String>
    ): Response<UserResponse>

    @DELETE("history/{id}")
    suspend fun deleteHistory(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>
    
    @GET("help")
    suspend fun getHelp(): Response<com.simats.dermacareai.network.models.HelpResponse>
    
    @GET("privacy")
    suspend fun getPrivacy(): Response<com.simats.dermacareai.network.models.PrivacyResponse>
}
