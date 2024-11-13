package LLD.MessagingManager.MessageObjectAdapter.ObjectTypes

import model.Message
import model.Notification

interface NotificationObjectInterface {
    fun getNotificationObject(message: Message):Notification
}