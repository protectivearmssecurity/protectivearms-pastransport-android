package com.live.pastransport.responseModel

import java.io.Serializable

data class OtpVerifyResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
):Serializable {
    data class Body(
        val __v: Int,
        val _id: String,
        val countryCode: String,
        val email: String,
        val driverType: String,
        val armedCertificates: String,
        val drivingLicense: String,
        val firstName: String,
        val image: String,
        val isAdminApproved: Int,
        val isAlreadyReg: Int,
        val isDuty: Int,
        val isNotificationEnabled: Int,
        val lastName: String,
        val otp: Int,
        val otpVerify: Int,
        val password: String,
        val phone: String,
        val role: Int,
        val status: Int,
        val token: String,
        val updatedAt: String,
    ):Serializable
}