package com.live.pastransport.responseModel

data class DriverPriceResponseModel(
    val body: List<Body?>?,
    val code: Int?,
    val message: String?,
    val success: Boolean?
) {
    data class Body(
        val __v: Int?,
        val _id: String?,
        val createdAt: String?,
        val driverType: String?,
        val price: String?,
        val updatedAt: String?
    )
}