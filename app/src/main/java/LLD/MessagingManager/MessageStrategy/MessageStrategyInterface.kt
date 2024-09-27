package LLD.MessagingManager.MessageStrategy

import com.example.personalmassenger.viewModel.MessagingViewModel
import com.google.firebase.firestore.QuerySnapshot
import model.Message

interface MessageStrategyInterface {
    fun send(message: Map<String,String>)
    fun receive(message: Message)
    fun delete()
}