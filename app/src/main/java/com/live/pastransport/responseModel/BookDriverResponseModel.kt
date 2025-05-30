package com.live.pastransport.responseModel

data class BookDriverResponseModel(
    val body: Body?,
    val code: Int?,
    val message: String?,
    val success: Boolean?
) {
    data class Body(
        val __v: Int?,
        val _id: String?,
        val companyName: String?,
        val createdAt: String?,
        val date: String?,
        val driveTime: Int?,
        val driverId: String?,
        val endLatitude: String?,
        val endLongitude: String?,
        val endTime: String?,
        val flightArivalTime: String?,
        val flightGateNumber: Int?,
        val flightName: String?,
        val flightNumber: String?,
        val locationType: Int?,
        val paymentDone: Int?,
        val price: Double?,
        val startLatitude: String?,
        val startLongitude: String?,
        val startTime: String?,
        val status: Int?,
        val tripEnd: String?,
        val tripStart: String?,
        val updatedAt: String?,
        val userId: String?
    )
}