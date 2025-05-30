package com.live.pastransport.responseModel

data class NotificationListResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val notifications: ArrayList<Notification>,
        val pagination: Pagination
    ) {
        data class Notification(
            val __v: Int,
            val _id: String,
            val bookingId: String,
            val createdAt: String,
            val message: String?,
            val receiverId: String,
            val senderId: SenderId,
            val status: Int,
            val type: Any,
            val updatedAt: String
        ) {
            data class SenderId(
                val __v: Int,
                val _id: String,
                val aboutUs: String,
                val address: String,
                val apple: String,
                val armedCertificates: String,
                val availablitiy: ArrayList<Any>,
                val avgRating: Int,
                val carModel: String,
                val companyName: String,
                val countryCode: String,
                val createdAt: String,
                val currentBookingId: Any,
                val customerId: String,
                val deviceToken: String,
                val deviceType: Int,
                val driverType: Int,
                val drivingLicense: String,
                val earnings: String,
                val email: String,
                val facebook: String,
                val firstName: String?,
                val google: String,
                val image: String?,
                val isAdminApproved: Int,
                val isDuty: Int,
                val isNotificationEnabled: Int,
                val lastName: String,
                val location: Location,
                val otp: Int,
                val otpVerify: Int,
                val password: String,
                val phone: String,
                val price: Int,
                val profileSetup: Int,
                val role: Int,
                val socialtype: String,
                val status: Int,
                val tripdetaile: List<Any>,
                val updatedAt: String,
                val walletAmount: Int
            ) {
                data class Location(
                    val coordinates: List<Double>,
                    val type: String
                )
            }
        }

        data class Pagination(
            val page: Int,
            val pageSize: Int,
            val totalCount: Int,
            val totalPages: Int
        )
    }
}