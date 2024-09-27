package LLD.ChatsManager

import Utils.Constants
import android.util.Log
import com.example.personalmassenger.fragments.Chats
import com.example.personalmassenger.localDatabse.localDbHandler
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import model.ChatInfo

class ChatsFacade(val context: Chats):ChatsDataInterface {

    override fun receiveMessages(docs: QuerySnapshot?) {
        val hash=HashMap<String,ChatInfo>()
        val unreadCount=HashMap<String,Int>()
        for (doc in docs?.documentChanges!!){
            if (doc.type !=DocumentChange.Type.ADDED) continue
            val message=doc.document.getString(Constants.KEY_TEXT).toString()
            val email=doc.document.getString(Constants.KEY_SENDER_EMAIL).toString()
            val timeStamp=doc.document.getString(Constants.KEY_TIME_STAMP).toString()
            val name=context.chatRecyclerAdapter.localDb.getContactName(email)
            unreadCount[email] = if(unreadCount[email]==null) 1 else unreadCount[email]!! + 1
            hash[email]=ChatInfo(name,"",Pair(message,timeStamp),email,email, unreadCount[email]!!,false)
        }

        for((key,value) in hash){
            updateChat(value)
        }
    }

    override fun updateChat(chatInfo: ChatInfo) {
        context.chatRecyclerAdapter.insertAtTop(chatInfo)
    }

    override fun newChat() {

    }

}