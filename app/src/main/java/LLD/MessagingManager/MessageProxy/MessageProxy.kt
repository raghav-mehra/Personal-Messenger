package LLD.MessagingManager.MessageProxy

import LLD.ChatsManager.ChatsFacade
import LLD.MessagingManager.MessageFacade.MessageFacade
import LLD.MessagingManager.MessageObjectAdapter.ObjectTypes.Image
import LLD.MessagingManager.MessageObjectAdapter.ObjectTypes.MessageObjectInterface
import LLD.MessagingManager.MessageStrategy.ImageStrategy
import LLD.MessagingManager.MessageStrategy.MessageStrategyInterface
import Utils.Constants
import com.example.personalmassenger.localDatabse.localDbHandler
import com.example.personalmassenger.viewModel.MessagingViewModel
import com.google.firebase.firestore.QuerySnapshot
import model.Message

class MessageProxy(messageViewModel: MessagingViewModel,localDb:localDbHandler) : MessageProxyInterface {
    val messageFacade = MessageFacade(messageViewModel,localDb)
    override fun send(message: Message) {
        messageFacade.send(message)
    }

    override fun receive(querySnapshot: QuerySnapshot) {
        messageFacade.receive(querySnapshot)
    }
}