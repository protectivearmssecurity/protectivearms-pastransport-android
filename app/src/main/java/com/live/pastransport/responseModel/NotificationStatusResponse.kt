package com.live.pastransport.responseModel

data class NotificationStatusResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val _id: String,
        val email: String,
        val firstName: String,
        val isNotificationEnabled: Int,
        val lastName: String
    )
}