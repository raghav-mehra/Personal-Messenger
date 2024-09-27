package LLD.MessagingManager.MessageObjectAdapter.ObjectTypes

import Utils.Constants
import com.google.firebase.firestore.DocumentChange
import model.Message

class Text : MessageObjectInterface {
    override fun getMessageObj(model: Message): Map<String, String> {
        val obj = mapOf<String, String>(
            Pair(Constants.KEY_TYPE, "Text"),
            Pair(Constants.KEY_TEXT, model.text),
            Pair(Constants.KEY_TIME_STAMP, model.timeStamp),
            Pair(Constants.KEY_SENDER_EMAIL,model.senderEmail),
            Pair(Constants.KEY_RECEIVER_EMAIL,model.receiverEmail),
            Pair(Constants.KEY_FILE_REFERENCE,model.fileReference)
        )
        return obj

    }

    override fun getMessageModel(documentChange: DocumentChange): Message {
        return Message(
            documentChange.document.get(Constants.KEY_TYPE).toString(),
            documentChange.document.get(Constants.KEY_TEXT).toString(),
            documentChange.document.get(Constants.KEY_TIME_STAMP).toString(),
            documentChange.document.get(Constants.KEY_SENDER_EMAIL).toString(),
            documentChange.document.get(Constants.KEY_RECEIVER_EMAIL).toString(),
            documentChange.document.get(Constants.KEY_FILE_REFERENCE).toString(),
        )
    }

    override fun getMessageModel(messageObj: Map<String, String>): Message {
        return Message(
            messageObj[Constants.KEY_TYPE].toString(),
            messageObj[Constants.KEY_TEXT].toString(),
            messageObj[Constants.KEY_TIME_STAMP].toString(),
            messageObj[Constants.KEY_SENDER_EMAIL].toString(),
            messageObj[Constants.KEY_RECEIVER_EMAIL].toString(),
            messageObj[Constants.KEY_FILE_REFERENCE].toString(),
        )
    }


}