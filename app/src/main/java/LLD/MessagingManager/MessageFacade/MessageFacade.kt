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
import android.widget.Toast
import com.example.personalmassenger.NotificationApi
import com.example.personalmassenger.localDatabse.localDbHandler
import com.example.personalmassenger.viewModel.MessagingViewModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import model.Message
import model.Notification
import model.NotificationData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            if(dc.document.get(Constants.KEY_SENDER_EMAIL).toString() == FirebaseUtil.currentUserEmail()){
                FirebaseUtil.currentUserChats().document(dc.document.id).delete()
                continue
            }
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

    fun sendNotification(message:Message) {
        FirebaseUtil.userDetails(message.receiverEmail).get().addOnSuccessListener {

             val notification=Notification(NotificationData(it.getString(Constants.KEY_TOKEN), hashMapOf("title" to message.senderEmail,"body" to message.text)))
            NotificationApi.create().sendNotification(notification)
                .enqueue(object :Callback<Notification>{
                    override fun onResponse(
                        call: Call<Notification>,
                        response: Response<Notification>
                    ) {
                        Log.d("Notification","Sent")
                    }

                    override fun onFailure(call: Call<Notification>, t: Throwable) {
                        Log.d("Notification","Not sent")
                    }
                })
        }
            .addOnFailureListener {

            }

    }

}
