package com.live.pastransport.repo

import com.live.pastransport.base.BASE_URL
import com.live.pastransport.network.RetrofitInterface
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject


class AuthRepo @Inject constructor(private var retrofitInterface: RetrofitInterface) {
    suspend fun getApiCallWithQueryParam(endPoint: String,map: HashMap<String, String>) = retrofitInterface.getApiCallWithQueryParam(BASE_URL + endPoint,map)

    suspend fun getApiCallWithParam(endPoint: String) = retrofitInterface.getApiCallWithParam(
        BASE_URL + endPoint)
    suspend fun postApiCallWithParam(map: HashMap<String, String>, endPoint: String) = retrofitInterface.postApiCallWithParam(map,
        BASE_URL + endPoint)
    suspend fun deleteApiCallWithParam(map: HashMap<String, String>) = retrofitInterface.deleteApiCallWithParam(map)
    suspend fun putApiCallWithParam(map: HashMap<String, String>, endPoint: String) = retrofitInterface.putApiCallWithParam(map,
        BASE_URL + endPoint)
    suspend fun putMultipartApiCall(map: ArrayList<MultipartBody.Part>, endPoint: String) = retrofitInterface.putMultipartApiCall(map,
        BASE_URL + endPoint)
    suspend fun postMultipartApiCall(map: ArrayList<MultipartBody.Part>, endPoint: String) = retrofitInterface.postMultipartApiCall(map,
        BASE_URL + endPoint)

    suspend fun postWithMultipartApiCall(image: MultipartBody.Part?, map: HashMap<String, RequestBody>, endPoint: String) = retrofitInterface.postWithMultipartApiCall(image, map,
        BASE_URL + endPoint)
    suspend fun postMultipartMapApiCall( partMap: Map<String, RequestBody>,map: ArrayList<MultipartBody.Part>, endPoint: String) = retrofitInterface.postMultipartMapApiCall(partMap,map, BASE_URL + endPoint)

    suspend fun postApiCallWithParamWithBaseUrl(map: HashMap<String, String>, completeUrl: String) = retrofitInterface.postApiCallWithParamWithBaseUrl(map,
        BASE_URL+ completeUrl)
}