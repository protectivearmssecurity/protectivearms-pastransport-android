package com.live.pastransport.viewModel

import android.content.Context
import com.live.pastransport.base.BaseViewModel
import com.live.pastransport.repo.AuthRepo
import com.live.pastransport.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepo: AuthRepo) : BaseViewModel() {
    fun getApiCallWithQueryParam(
        context: Context,
        endPoint: String,
        type: String = "",
        map: HashMap<String, String>,
        showLoader: Boolean = true
    ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { endPoint }
            makeApiCall(
                apiCall = { authRepo.getApiCallWithQueryParam(endPoint,map) },
                key = key,
                context = context,
                showLoader = showLoader

            )
        } else {
            // open full screen no internet dialog
            Utils.showNoInternetDialog(context) {
                getApiCallWithQueryParam(context, endPoint, type, map,showLoader)
            }
        }

    }
    fun makeGetApiCall(
        context: Context,
        endPoint: String,
        type: String = "",
        showLoader: Boolean = true
    ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { endPoint }
            makeApiCall(
                apiCall = { authRepo.getApiCallWithParam(endPoint) },
                key = key,
                context = context,
                showLoader = showLoader
            )
        } else {
            // open full screen no internet dialog
            Utils.showNoInternetDialog(context) {
                makeGetApiCall(context, endPoint, type, showLoader)
            }
        }

    }

    fun makPostApiCall(
        context: Context,
        endPoint: String,
        type: String = "",
        showLoader: Boolean = true,
        map: HashMap<String, String>
    ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { endPoint }
            makeApiCall(
                apiCall = { authRepo.postApiCallWithParam(map, endPoint) },
                key = key,
                context = context,
                showLoader = showLoader
            )
        } else {
            Utils.showNoInternetDialog(context) {
                makPostApiCall(context, endPoint, type, showLoader,map)
            }
        }

    }

    fun postApiCallWithParamWithBaseUrl(
        context: Context,
        completeUrl: String,
        type: String = "",
        showLoader: Boolean = true,
        map: HashMap<String, String>

    ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { completeUrl }
            makeApiCall(
                apiCall = { authRepo.postApiCallWithParamWithBaseUrl(map, completeUrl) },
                key = key,
                context = context,
                showLoader = showLoader
            )
        } else {
            Utils.showNoInternetDialog(context) {
                postApiCallWithParamWithBaseUrl(context, completeUrl, type, showLoader,map)
            }
        }

    }


    fun deleteApiCallWithParam(
        context: Context,
        endPoint: String,
        type: String = "",
        showLoader: Boolean = true,
        map: HashMap<String, String>

    ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { endPoint }
            makeApiCall(
                apiCall = { authRepo.deleteApiCallWithParam(map) },
                key = key,
                context = context,
                showLoader = showLoader
            )
        } else {
            Utils.showNoInternetDialog(context) {
                deleteApiCallWithParam(context, endPoint, type, showLoader,map)
            }
        }

    }
    fun putApiCallWithParam(
        context: Context,
        endPoint: String,
        type: String = "",
        showLoader: Boolean = true,
        map: HashMap<String, String>

    ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { endPoint }
            makeApiCall(
                apiCall = { authRepo.putApiCallWithParam(map, endPoint) },
                key = key,
                context = context,
                showLoader = showLoader
            )
        } else {
            Utils.showNoInternetDialog(context) {
                putApiCallWithParam(context, endPoint, type, showLoader,map)
            }
        }

    }
    fun makPutMultipartApiCall(
        context: Context,
        endPoint: String,
        type: String = "",
        showLoader: Boolean = true,
        map: ArrayList<MultipartBody.Part>

    ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { endPoint }
            makeApiCall(
                apiCall = { authRepo.putMultipartApiCall(map, endPoint) },
                key = key,
                context = context,
                showLoader = showLoader
            )
        } else {
            Utils.showNoInternetDialog(context) {
                makPutMultipartApiCall(context, endPoint, type, showLoader,map)
            }
        }

    }
    fun postMultipartApiCall(
        context: Context,
        endPoint: String,
        type: String = "",
        showLoader: Boolean = true,
        map: ArrayList<MultipartBody.Part>

    ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { endPoint }
            makeApiCall(
                apiCall = { authRepo.postMultipartApiCall(map, endPoint) },
                key = key,
                context = context,
                showLoader = showLoader
            )
        } else {
            Utils.showNoInternetDialog(context) {
                postMultipartApiCall(context, endPoint, type, showLoader,map)
            }
        }

    }
    fun postMultipartMapApiCall(
        context: Context,
        endPoint: String,
        type: String = "",
        showLoader: Boolean = true,
        partMap: Map<String, RequestBody>,
        map: ArrayList<MultipartBody.Part>

    ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { endPoint }
            makeApiCall(
                apiCall = { authRepo.postMultipartMapApiCall(partMap,map, endPoint) },
                key = key,
                context = context,
                showLoader = showLoader
            )
        } else {
            Utils.showNoInternetDialog(context) {
                postMultipartMapApiCall(context, endPoint, type, showLoader,partMap,map)
            }
        }

    }
    fun postWithMultipartApiCall(
        context: Context,
        endPoint: String,
        type: String = "",
        showLoader: Boolean = true,
        image: MultipartBody.Part?,
        maps: HashMap<String, RequestBody>

        ) {
        if (Utils.internetAvailability(context)) {
            val key = type.ifEmpty { endPoint }
            makeApiCall(
                apiCall = { authRepo.postWithMultipartApiCall(image, maps,endPoint) },
                key = key,
                context = context,
                showLoader = showLoader
            )
        } else {
            Utils.showNoInternetDialog(context) {
                postWithMultipartApiCall(context, endPoint, type, showLoader, image, maps)
            }
        }

    }


}