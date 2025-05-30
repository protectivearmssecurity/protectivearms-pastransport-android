package com.live.pastransport.responseModel

data class PaymentIntentResponse(
    val body: Body,
    val code: Int,
    val message: String,
    val success: Boolean
) {
    data class Body(
        val customer: String,
        val ephemeralKey: String,
        val paymentIntent: String,
        val publishableKey: String,
        val transactionId: String
    )
}