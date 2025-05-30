package com.live.pastransport.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.live.pastransport.R
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.live.pastransport.utils.CustomProgressDialog
import com.live.pastransport.network.StatusType
import com.live.pastransport.network.UIEventManager
import com.live.pastransport.utils.goToSelectUserWithClearFlag
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

open class BaseViewModel : ViewModel(), UIEventManager {

    @SuppressLint("StaticFieldLeak")
    var mContext: Context? = null
    private val progressDialog by lazy {
        CustomProgressDialog(Dialog(mContext!!))
    }

    val liveDataMap = MutableLiveData<ResourceModel>()

    private fun <T> postLiveData(key: String, value: T?) {
        try {
            liveDataMap.value = ResourceModel(key, value as JsonElement?, StatusType.SUCCESS)
        } catch (e: Exception) {
            Log.d("postLiveData", "postLiveData: " + e.message)
        }
    }

    override fun showProgress() {
        progressDialog.show(mContext!!)
    }

    override fun hideProgress() {

        progressDialog.hide()
    }


    protected fun makeApiCall(
        apiCall: suspend () -> JsonElement,
        key: String,
        context: Context,
        showLoader: Boolean = true
    ) {
        mContext = context
        if (showLoader) {
            showProgress()
        } else {
            hideProgress()
        }
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
        }) {
            try {
                val response = apiCall.invoke()
                hideProgress()
                postLiveData(key, response)
            } catch (e: Exception) {
                e.printStackTrace()
                hideProgress()

                Log.e("ApiCalls", "Call error: ${e.localizedMessage}", e.cause)
                when (e) {
                    is HttpException -> {
                        val body = e.response()?.errorBody()
                        val msg = getErrorMessage(body)
                        if (e.code() == 400) {
                            liveDataMap.value =
                                ResourceModel(key, null, StatusType.ERROR, msg.toString())

                        }
                        else  if (e.code() == 402) {

                            liveDataMap.value =
                                ResourceModel(key, null, StatusType.ERROR, msg.toString())
                        }
                        else  if (e.code() == 401) {
                            liveDataMap.value = ResourceModel(
                                key,
                                null,
                                StatusType.ERROR,
                                context.getString(R.string.session_expired)
                            )
                            launch {
                                delay(1000)
                                context.goToSelectUserWithClearFlag()
                            }
                        }

                        else  if (e.code() == 403) {
                            liveDataMap.value = ResourceModel(
                                key,
                                null,
                                StatusType.ERROR,
                                msg.toString()
                            )
                        } else if (e is SocketTimeoutException) {
                            liveDataMap.value = ResourceModel(
                                key,
                                null,
                                StatusType.ERROR,
                                context.getString(R.string.connection_timed_out)
                            )
                        } else if (e is IOException) {
                            liveDataMap.value = ResourceModel(
                                key,
                                null,
                                StatusType.ERROR,
                                context.getString(R.string.cannot_reach_server)
                            )
                             ResourceModel(key, null, StatusType.ERROR, msg.toString())

                        }}


                    }
                null
            }
        }
    }

    private fun getErrorMessage(responseBody: ResponseBody?): String {
        val errorBodyString = responseBody?.string()
        var errorMessage = ""

        errorBodyString?.let {
            try {
                val jsonError = JsonParser.parseString(it).asJsonObject
                errorMessage = jsonError.getAsJsonPrimitive("message").asString
            } catch (e: JsonSyntaxException) {
                Log.d("getErrorMessage", "Error parsing JSON: ${e.message}")
            }
        }

        return errorMessage
    }


}


