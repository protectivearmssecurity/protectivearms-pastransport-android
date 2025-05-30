package com.live.pastransport.responseModel

data class FileUploadResponse(
    val body: ArrayList<String>,
    val code: Int,
    val message: String,
    val success: Boolean
)