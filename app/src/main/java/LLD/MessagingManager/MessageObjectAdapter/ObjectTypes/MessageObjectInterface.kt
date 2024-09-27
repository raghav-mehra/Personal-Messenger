package LLD.MessagingManager.MessageObjectAdapter.ObjectTypes

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import model.Message

interface MessageObjectInterface {

    fun getMessageObj(model:Message) : Map<String,String>
    fun getMessageModel(documentChange: DocumentChange): Message
    fun getMessageModel(messageObj:Map<String,String>): Message

}