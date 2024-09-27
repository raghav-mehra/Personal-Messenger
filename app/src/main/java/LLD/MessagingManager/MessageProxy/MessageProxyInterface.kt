package LLD.MessagingManager.MessageProxy

import com.google.firebase.firestore.QuerySnapshot
import model.Message

interface MessageProxyInterface {
    fun send(message: Message)
    fun receive(querySnapshot: QuerySnapshot)
}