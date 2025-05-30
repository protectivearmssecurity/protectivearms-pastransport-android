package com.live.pastransport.network

import com.google.gson.JsonElement
import com.live.pastransport.base.BASE_URL
import com.live.pastransport.base.LOGIN
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*


interface RetrofitInterface {
    @GET
    suspend fun getApiCallWithParam(@Url endPoint: String): JsonElement

    @GET
    suspend fun getApiCallWithQueryParam(@Url endPoint: String,@QueryMap params: HashMap<String, String>): JsonElement

    @FormUrlEncoded
    @POST
    suspend fun postApiCallWithParam(
        @FieldMap params: HashMap<String, String>,
        @Url endPoint: String
    ): JsonElement

    @FormUrlEncoded
    @POST
    suspend fun postApiCallWithQueryMap(
        @QueryMap params: HashMap<String, String>,
        @Url endPoint: String
    ): JsonElement

    //    @DELETE
//    suspend fun deleteApiCallWithParam(
//        @Url endPoint: String,
//        @QueryMap params: Map<String, String>,
//    ): JsonElement
    @FormUrlEncoded
    @HTTP(method = "DELETE", path = BASE_URL + LOGIN, hasBody = true)
    suspend fun deleteApiCallWithParam(
        @FieldMap params: Map<String, String>
    ): JsonElement


    @FormUrlEncoded
    @POST // No URL in annotation, use @Url to pass the complete URL
    suspend fun postApiCallWithParamWithBaseUrl(
        @FieldMap params: HashMap<String, String>,
        @Url completeUrl: String // Dynamic URL passed here
    ): JsonElement
    @Multipart
    @POST
    suspend fun postMultipartMapApiCall(
        @PartMap partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part params: ArrayList<MultipartBody.Part>,
        @Url endPoint: String
    ): JsonElement


    @Multipart
    @PUT
    suspend fun putMultipartApiCall(
        @Part params: ArrayList<MultipartBody.Part>,
        @Url endPoint: String
    ): JsonElement

    @Multipart
    @POST
    suspend fun postMultipartApiCall(
        @Part params: ArrayList<MultipartBody.Part>,
        @Url endPoint: String
    ): JsonElement

    @Multipart
    @POST
    suspend fun postWithMultipartApiCall(
        @Part image: MultipartBody.Part?,
        @PartMap map: HashMap<String, RequestBody>,
        @Url endPoint: String
    ): JsonElement

    @FormUrlEncoded
    @PUT
    suspend fun putApiCallWithParam(
        @FieldMap params: HashMap<String, String>,
        @Url endPoint: String
    ): JsonElement
}
