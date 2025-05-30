package com.live.pastransport.responseModel

data class CmsResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val __v: Int,
        val _id: String,
        val createdAt: String,
        val description: String,
        val role: String,
        val title: String,
        val updatedAt: String
    )
}