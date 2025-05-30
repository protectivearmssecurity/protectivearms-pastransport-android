package com.live.pastransport.responseModel


data class MessagesModel(
    val blockByHim: Int? = null,
    val blockByMe: Int? = null,
    val code: Int?,
    val getdata: ArrayList<Getdata>? = null,
    val success_message: String?,
    val unread_message_count: Int? = null
) {
    data class Getdata(
        val __v: Int? = null,
        val _id: String? = null,
        val constantId: String? = null,
        var createdAt: String? = null,
        val is_read: Int? = null,
        var message: String? = null,
        val messageSide: String? = null,
        val message_type: String? = null,
        var receiverId: ReceiverId? = null,
        var senderId: SenderId? = null,
        var updatedAt: String? = null
    ) {
        data class ReceiverId(
            var _id: String? = null,
            var firstName: String? = null,
            var image: String? = null,
            var lastName: String? = null
        )

        data class SenderId(
            var _id: String? = null,
            var firstName: String? = null,
            var image: String? = null,
            var lastName: String? = null
        )
    }
}