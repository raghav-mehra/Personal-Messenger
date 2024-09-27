package LLD.ChatsManager

import com.google.firebase.firestore.QuerySnapshot
import model.ChatInfo

interface ChatsDataInterface {
    fun receiveMessages(docs: QuerySnapshot?)
    fun updateChat(chatInfo: ChatInfo)
    fun newChat()
}