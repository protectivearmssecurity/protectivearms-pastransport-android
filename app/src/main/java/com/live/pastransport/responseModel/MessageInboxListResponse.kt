package com.live.pastransport.responseModel


data class MessageInboxListResponse(
    val code: Int?,
    val getdata: ArrayList<Getdata>,
    val success_message: String?
) {
    data class Getdata(
        val __v: Int?,
        val _id: String?,
        val createdAt: String?,
        val deletedLastMessageId: Int?,
        val is_block: Int?,
        val lastmessage: Lastmessage?,
        val receiverId: ReceiverId?,
        val senderId: SenderId?,
        val unreadCount: Int?,
        val updatedAt: String?
    ) {
        data class Lastmessage(
            val _id: String?,
            val is_read: Int?,
            val message: String?,
            val message_type: String?,
            val receiverId: String?,
            val senderId: String?,
            val updatedAt: String?
        )

        data class ReceiverId(
            val _id: String?,
            val email: String?,
            val firstName: String?,
            val image: String?,
            val lastName: String?
        )

        data class SenderId(
            val _id: String?,
            val email: String?,
            val firstName: String?,
            val image: String?,
            val lastName: String?
        )
    }
}
