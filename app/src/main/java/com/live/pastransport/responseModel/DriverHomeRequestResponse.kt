package com.live.pastransport.responseModel

import java.io.Serializable

data class DriverHomeRequestResponse(
    val body: ArrayList<Body>,
    val code: Int,
    val message: String,
    val success: Boolean
):Serializable {
    data class Body(
        val __v: Int,
        val _id: String,
        val companyName: String,
        val createdAt: String,
        val date: String,
        val driveTime: Int,
        val driverId: DriverId,
        val endLatitude: String,
        val endLongitude: String,
        val endTime: String,
        val flightArivalTime: String,
        val flightGateNumber: Int,
        val flightName: String,
        val flightNumber: String,
        val locationType: Int,
        val paymentDone: Int,
        val price: Double,
        val startLatitude: String,
        val startLongitude: String,
        val startTime: String,
        val status: Int,
        val tripEnd: String,
        val tripStart: String,
        val updatedAt: String,
        val userId: UserId
    ):Serializable {
        data class DriverId(
            val _id: String,
            val firstName: String,
            val image: String,
            val lastName: String
        ):Serializable

        data class UserId(
            val _id: String,
            val firstName: String,
            val image: String,
            val lastName: String
        ):Serializable
    }
}