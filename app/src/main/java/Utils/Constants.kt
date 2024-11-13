package Utils

import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Constants {
    val KEY_IMAGE="Image"
    val KEY_DOCUMENT="Document"
    val KEY_FILE_REFERENCE="fileReference"
    val KEY_RECEIVER_EMAIL="receiverEmail"
    val KEY_SENDER_EMAIL="senderEmail"
    val KEY_TEXT="Text"
    val KEY_TYPE="type"
    val KEY_TOKEN="token"
    val KEY_BUNDLE="Bundle"
    val KEY_REFERENCE="Reference"
    val KEY_IMAGE_URI="imageUri"
    val KEY_DATE="Date"
    val KEY_IMAGEPATH="imagePath"
    val KEY_TABLE_NAME="tableName"
    val KEY_USERNAME="userName"
    val KEY_SENDER_ID="senderId"
    val KEY_ID="Id"
    val KEY_UID="uid"
    val KEY_EMAIL="Email"
    val KEY_CONTACTS="Contacts"
    val KEY_PROFILE="Profile"
    val KEY_OPTION="selectedOption"
    val KEY_TIME_STAMP="timeStamp"
    val KEY_PROFILE_PHOTO="profilePhoto"
    val KEY_MESSAGE="message"
    val DATABASE_NAME= generateDatabaseName()
    const val DATABASE_VERSION=1
    val TABLE_NAME_CONTACTS="contacts"
    val TABLE_NAME_CHATS="chats"
    const val KEY_NAME="name"
    const val KEY_PHONE_NUMBER="phoneNumber"

    fun generateDatabaseName():String{
        var name="storemsg"
        var i=0
        val email=FirebaseAuth.getInstance().currentUser?.email.toString()
        while (email[i]!='@'){
            name+=email[i]
            i++
        }
        return name+".db"
    }
    fun getTableName(email:String):String{
        var name=""
        var i=0
        while(i<email.length && email[i]!='@'){
            name+=email[i++]
        }
        return name
    }
    fun getFormattedTime(timeStamp:String):String{
        return timeStamp.slice(IntRange(11,15))
    }
    fun isOtherDay(timeStamp1: String,timeStamp2:String):Boolean{

        return timeStamp1.slice(IntRange(0,10)) != timeStamp2.slice(IntRange(0,10))
    }

    fun compareDay(timeStamp: String): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val currentTimeStamp = current.format(formatter)
        if(timeStamp.slice(IntRange(0,10)) == currentTimeStamp.slice(IntRange(0,10))){
            return "today"
        }
        return timeStamp.slice(IntRange(0,10))
    }
}