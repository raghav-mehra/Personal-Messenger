package model

import android.provider.ContactsContract.CommonDataKinds.Email
import com.google.firebase.firestore.DocumentReference

data class ChatInfo(
    val name:String,
    val profilePhoto: String,
    val lastMessage:Pair<String,String>,
    val email: String,
    val uid:String
)
