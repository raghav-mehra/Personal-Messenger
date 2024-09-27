package LLD.MessagingManager.MessageObjectAdapter

import LLD.MessagingManager.MessageObjectAdapter.ObjectTypes.Image
import LLD.MessagingManager.MessageObjectAdapter.ObjectTypes.MessageObjectInterface
import LLD.MessagingManager.MessageObjectAdapter.ObjectTypes.Text
import Utils.Constants
import com.google.firebase.firestore.DocumentChange
import model.Message

class MessageObjectAdapter () {


    private lateinit var mapObj:Map<String,String>
    private lateinit var messageInterface:MessageObjectInterface
    private var objects: List<MessageObjectInterface> = listOf(Text(), Image())


    fun produceMessageObj(message: Message):Map<String,String>{
        when (message.type) {
            Constants.KEY_TEXT -> {
                messageInterface=objects[0]
            }

            Constants.KEY_IMAGE -> {
                messageInterface=objects[1]
            }
        }
        return messageInterface.getMessageObj(message)
    }

    fun getMessageModel(documentChange: DocumentChange):Message{
        when (documentChange.document.get(Constants.KEY_TYPE).toString()) {
            Constants.KEY_TEXT -> {
                messageInterface = objects[0]
            }

            Constants.KEY_IMAGE -> {
                messageInterface = objects[1]
            }
        }
        return messageInterface.getMessageModel(documentChange)
    }
    fun getMessageModel(messageObj:Map<String,String>):Message{
        when (messageObj[Constants.KEY_TYPE].toString()) {
            Constants.KEY_TEXT -> {
                messageInterface = objects[0]
            }

            Constants.KEY_IMAGE -> {
                messageInterface = objects[1]
            }
        }
        return messageInterface.getMessageModel(messageObj)
    }


}