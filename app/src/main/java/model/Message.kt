package model

import LLD.MessagingManager.MessageObjectAdapter.ObjectTypes.MessageObjectInterface
import LLD.MessagingManager.MessageStrategy.MessageStrategyInterface

data class Message(
    val type:String,
    val text: String,
    val timeStamp: String,
    val senderEmail: String,
    val receiverEmail: String,
    val fileReference: String
)
