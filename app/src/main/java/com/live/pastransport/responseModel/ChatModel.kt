package com.live.pastransport.responseModel

data class ChatModel(
    val __v: Int?,
    val _id: String?,
    val constantId: String?,
    val createdAt: String? = null,
    val is_read: Int?,
    val message: String?,
    val message_type: String?,
    val receiverId: ReceiverId?,
    val senderId: SenderId?,
    val updatedAt: String?
) {
    data class ReceiverId(
        val _id: String?,
        val firstName: String?,
        val image: String?,
        val lastName: String?
    )

    data class SenderId(
        val _id: String?,
        val firstName: String?,
        val image: String?,
        val lastName: String?
    )
}