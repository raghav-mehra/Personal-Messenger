package LLD.MessagingManager.MessageFacade

import LLD.MessagingManager.MessageObjectAdapter.MessageObjectAdapter
import LLD.MessagingManager.MessageObjectAdapter.ObjectTypes.Image
import LLD.MessagingManager.MessageObjectAdapter.ObjectTypes.MessageObjectInterface
import LLD.MessagingManager.MessageObjectAdapter.ObjectTypes.Text
import LLD.MessagingManager.MessageStrategy.ImageStrategy
import LLD.MessagingManager.MessageStrategy.MessageStrategyInterface
import LLD.MessagingManager.MessageStrategy.TextStrategy
import Utils.Constants
import Utils.FirebaseUtil
import android.util.Log
import com.example.personalmassenger.localDatabse.localDbHandler
import com.example.personalmassenger.viewModel.MessagingViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import model.Message

class MessageFacade(private val viewModel: MessagingViewModel,private val localDb:localDbHandler) {

    private var messageObjAdapter: MessageObjectAdapter = MessageObjectAdapter()
    private lateinit var messageStrategy: MessageStrategyInterface
    private var strategies: List<MessageStrategyInterface> = listOf(TextStrategy(viewModel,localDb), ImageStrategy())
    private var objects: List<MessageObjectInterface> = listOf(Text(), Image())
    fun send(message: Message) {

        when (message.type) {
            Constants.KEY_TEXT -> {

                messageStrategy = strategies[0]
                messageStrategy.send(messageObjAdapter.produceMessageObj(message))

            }

            Constants.KEY_IMAGE -> {

                messageStrategy = strategies[1]
                messageStrategy.send(messageObjAdapter.produceMessageObj(message))

            }

        }

    }

    fun receive(querySnapshot: QuerySnapshot) {
        for (dc in querySnapshot.documentChanges) {
            if(dc.type != DocumentChange.Type.ADDED) continue
            Log.d("Received_Message",dc.document.get(Constants.KEY_TEXT).toString())
            when (dc.document.get(Constants.KEY_TYPE).toString()) {
                Constants.KEY_TEXT -> {
                    messageStrategy = strategies[0]
                    messageStrategy.receive(messageObjAdapter.getMessageModel(dc))
                    FirebaseUtil.currentUserChats().document(dc.document.id).delete()

                }
                Constants.KEY_IMAGE -> {
                    messageStrategy = strategies[1]
                    messageStrategy.receive(messageObjAdapter.getMessageModel(dc))
                    FirebaseUtil.currentUserChats().document(dc.document.id).delete()
                }
            }

        }

    }

}
