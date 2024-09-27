package Utils

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseUtil {
    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private val auth= FirebaseAuth.getInstance()
    fun currentUserDetails():DocumentReference{
       return db.collection("Users").document(auth.currentUser?.email!!)
    }
    fun currentUserId():String{
        return auth.currentUser?.uid!!
    }
    fun currentUserEmail():String{
        return auth.currentUser?.email!!
    }
    fun currentUserChats(): CollectionReference {
        return db.collection("Chats").document(auth.currentUser?.email!!).collection("Message")
    }
    fun userDetails(email:String):DocumentReference{
        return db.collection("Users").document(email)
    }
    fun firebaseAuth(): FirebaseAuth {
        return auth
    }
    fun userChats(id:String):CollectionReference{
        return  db.collection("Chats").document(id)
            .collection("Message")
    }
    fun getStorageReference():StorageReference{
        return storageReference
    }
    fun profilePicReference(email:String) :StorageReference{
        return storageReference.child("profile_photos/$email")
    }


}