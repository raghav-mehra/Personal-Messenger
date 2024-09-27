package LLD.MessagingManager.MessageStrategy

import Utils.Constants
import Utils.FirebaseUtil
import android.util.Log
import com.example.personalmassenger.localDatabse.localDbHandler
import com.example.personalmassenger.viewModel.MessagingViewModel
import com.google.firebase.firestore.QuerySnapshot
import model.Message

class TextStrategy(private val viewModel: MessagingViewModel,private val localDb:localDbHandler):MessageStrategyInterface {


    override fun send(message: Map<String, String>) {
        FirebaseUtil.userChats(message[Constants.KEY_RECEIVER_EMAIL].toString()).add(message).addOnSuccessListener {
            viewModel.addMessage(message)
        }
    }

    override fun receive(message: Message) {
        viewModel.addMessage(message)
    }

    override fun delete() {

    }
}