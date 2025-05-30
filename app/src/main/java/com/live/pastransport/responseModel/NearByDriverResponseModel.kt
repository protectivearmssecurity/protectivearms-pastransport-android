package com.live.pastransport.responseModel

data class NearByDriverResponseModel(
    val body: List<Body?>?,
    val code: Int?,
    val message: String?,
    val success: Boolean?
) {
    data class Body(
        val __v: Int?,
        val _id: String?,
        val aboutUs: String?,
        val address: String?,
        val apple: String?,
        val armedCertificates: String?,
        val availablitiy: List<Any?>?,
        val avgRating: Double?,
        val carModel: String?,
        val companyName: String?,
        val countryCode: String?,
        val createdAt: String?,
        val currentBookingId: Any?,
        val customerId: String?,
        val deviceToken: String?,
        val deviceType: Int?,
        val dist: Dist?,
        val driverType: Int?,
        val drivingLicense: String?,
        val earnings: String?,
        val email: String?,
        val facebook: String?,
        val firstName: String?,
        val google: String?,
        val image: String?,
        val isAdminApproved: Int?,
        val isDuty: Int?,
        val isNotificationEnabled: Int?,
        val lastName: String?,
        val location: Location?,
        val otp: Int?,
        val otpVerify: Int?,
        val password: String?,
        val phone: String?,
        val price: Int?,
        val profileSetup: Int?,
        val role: Int?,
        val socialtype: String?,
        val status: Int?,
        val tripdetaile: List<Any?>?,
        val updatedAt: String?,
        val walletAmount: Int?
    ) {
        data class Dist(
            val calculated: Double?,
            val location: Location?
        ) {
            data class Location(
                val coordinates: List<Double?>?,
                val type: String?
            )
        }

        data class Location(
            val coordinates: List<Double?>?,
            val type: String?
        )
    }
}