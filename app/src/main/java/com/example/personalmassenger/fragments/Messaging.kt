package com.example.personalmassenger.fragments

import Utils.Constants
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.currentComposer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.example.personalmassenger.Communicator
import com.example.personalmassenger.R
import com.example.personalmassenger.adapters.MessageAdapter
import com.example.personalmassenger.localDatabse.localDbHandler
import com.example.personalmassenger.viewModel.NewViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import model.ChatInfo
import model.Message
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.Calendar

class Messaging : Fragment() {

    private lateinit var communicator: Communicator
    private lateinit var recyclerview: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageEditText: EditText
    private lateinit var messageSendButton: FloatingActionButton
    private lateinit var toolbar_title:TextView
    private lateinit var toolbar:Toolbar
    private lateinit var toolbar_profile_pic:ImageView
    private lateinit var db: FirebaseFirestore
    private  var storageReference:StorageReference= FirebaseStorage.getInstance().reference
    private lateinit var secondPersonReference: CollectionReference
    private lateinit var myReference: CollectionReference
    private lateinit var localDb:localDbHandler
    private lateinit var auth: FirebaseAuth
    lateinit var viewModel: NewViewModel
    private val register=registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
        if (it!=null){
        uploadImage(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel= ViewModelProvider(this).get(NewViewModel::class.java)
        val receiverId = arguments?.getString(Constants.KEY_ID).toString()
        val receiverEmail=arguments?.getString(Constants.KEY_EMAIL).toString()
        val view = inflater.inflate(R.layout.fragment_user_chat, container, false)
        val messageList=localDbHandler(activity?.applicationContext!!).getFullChat(receiverId)
        Log.d("receiverEmail",receiverEmail)
        auth = FirebaseAuth.getInstance()
        recyclerview = view.findViewById(R.id.message_recycler)
        localDb=localDbHandler(activity?.applicationContext!!)
        messageEditText = view.findViewById(R.id.fragment_message_edit_text)
        messageSendButton = view.findViewById(R.id.message_send_button)
        messageAdapter = MessageAdapter()
        toolbar_profile_pic=view.findViewById(R.id.toolbar_profile_pic)
        toolbar=view.findViewById(R.id.messaging_toolbar)
        toolbar_title=view.findViewById(R.id.toolbar_user_name)
        toolbar_title.text=arguments?.getString(Constants.KEY_USERNAME)
        recyclerview.adapter = messageAdapter
        val manager=LinearLayoutManager(activity)
        recyclerview.layoutManager = manager
        db = FirebaseFirestore.getInstance()
        messageAdapter.updateMessages(messageList)
        messageAdapter.registerAdapterDataObserver(object:AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerview.smoothScrollToPosition(itemCount-1)
            }
        })
//        db.collection("Users").document("raghav@gmail.com").get().addOnSuccessListener{
//            val newUrl=it.getString(Constants.KEY_PROFILE_PHOTO).toString()
//            Log.d("url",newUrl)
//            if (newUrl.isNotEmpty()){
//            toolbar_profile_pic.setImageBitmap(BitmapFactory.decodeStream(URL(newUrl).openStream()))
//            }
//            //Picasso.get().load(newUrl).into(toolbar_profile_pic)
//        }
        recyclerview.scrollToPosition(messageList.size-1)
        messageSendButton.setOnClickListener {
            sendMessage()
        }

        messageEditText.setOnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2
            val DRAWABLE_LEFT = 0
            val DRAWABLE_TOP = 1
            val DRAWABLE_BOTTOM = 3
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (messageEditText.right - messageEditText.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    messageEditText.setText("")
                    val popupMenu=PopupMenu(activity?.applicationContext,messageEditText)
                    popupMenu.menuInflater.inflate(R.menu.popup_menu,popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        when(item.itemId){
                            R.id.action_image->{
                                register.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            R.id.action_document->{

                            }

                        }
                        true
                    })
                    popupMenu.show()
                }
            }
            false
        }
        secondPersonReference =
            db.collection("Chats").document(receiverId)
                .collection("Message")

        myReference = db.collection("Chats").document(auth.currentUser!!.uid).collection("Message")

        myReference.whereEqualTo(Constants.KEY_SENDER_ID,receiverId).orderBy(Constants.KEY_TIME_STAMP).addSnapshotListener { snapshot, error ->

            error?.let {
                Toast.makeText(activity,it.message,Toast.LENGTH_SHORT).show()
                Log.d("snapshot error: ",it.message.toString())
            }

            snapshot?.let {
                for(dc in it.documentChanges){
                    val receivedMessage= dc.document.get(Constants.KEY_MESSAGE)
                    val receivedTime = dc.document.get(Constants.KEY_TIME_STAMP)
                    val receivedEmail = dc.document.get(Constants.KEY_EMAIL)
                    val receivedImagePath=dc.document.get(Constants.KEY_IMAGEPATH).toString()
                    Log.d("Received Message",receivedMessage.toString())
                    if(dc.type == DocumentChange.Type.REMOVED){
                        continue
                    }
                    localDb.addMessage(receiverId,Message(
                        receiverId,
                        receivedMessage.toString(),
                        receivedTime.toString(),receivedEmail.toString(),receivedImagePath
                    ))
                    messageAdapter.addMessage(
                        Message(
                            receiverId,
                            receivedMessage.toString(),
                            receivedTime.toString(),receivedEmail.toString(),receivedImagePath
                        )
                    )
                    myReference.document(dc.document.id).delete()
                    viewModel.chatChanges.value = ++viewModel.initial

                }
            }
        }
        return view
    }

    private fun sendMessage() {
        val receiverId = arguments?.getString(Constants.KEY_ID).toString()
        val message = messageEditText.text.toString().trim()
        if (message.isEmpty()) {
            messageEditText.error = "Enter some message to send"
        } else {
            val calender = Calendar.getInstance()
            val hour = calender.get(Calendar.HOUR_OF_DAY)
            val minute = calender.get(Calendar.MINUTE)
            val timeStamp = "${calender.get(Calendar.DATE)} $hour:$minute"
            val messageObj = mutableMapOf<String, String>().also {
                it[Constants.KEY_SENDER_ID] = auth.currentUser?.uid!!.toString()
                it[Constants.KEY_MESSAGE] = message
                it[Constants.KEY_TIME_STAMP] = timeStamp
                it[Constants.KEY_EMAIL]= auth.currentUser?.email!!
                it[Constants.KEY_IMAGEPATH]="null"
            }
            secondPersonReference.add(messageObj).addOnSuccessListener {
                val senderMessage= Message(
                    messageObj[Constants.KEY_SENDER_ID].toString(),
                    messageObj[Constants.KEY_MESSAGE].toString(),
                    messageObj[Constants.KEY_TIME_STAMP].toString(),
                    messageObj[Constants.KEY_EMAIL].toString(),
                    messageObj[Constants.KEY_IMAGEPATH].toString()
                )
                localDb.addMessage(receiverId,senderMessage)
                messageAdapter.addMessage(senderMessage)
                recyclerview.smoothScrollToPosition(messageAdapter.getLastPosition())
                messageEditText.text.clear()
                viewModel.chatChanges.value = ++viewModel.initial
                Log.d("value",viewModel.chatChanges.value.toString())
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        communicator=activity as Communicator
        communicator.refreshChats()
    }
    private fun uploadImage(uri:Uri?){
        val receiverId = arguments?.getString(Constants.KEY_ID).toString()
        val userId=auth.currentUser?.uid!!
//        val mediaNum=db.collection("Chats").document(receiverId)
//            .collection("MediaNumber").get().addOnSuccessListener {
//                it.documents.get()
//            }
        val imagePath="chat_images/$userId/${System.currentTimeMillis()}"
        storageReference.child(imagePath).putFile(uri!!).addOnSuccessListener {
            val calender = Calendar.getInstance()
            val hour = calender.get(Calendar.HOUR_OF_DAY)
            val minute = calender.get(Calendar.MINUTE)
            val timeStamp = "$hour:$minute"
            val messageObj = mutableMapOf<String, String>().also {
                it[Constants.KEY_SENDER_ID] =auth.currentUser?.uid!!.toString()
                it[Constants.KEY_MESSAGE] = ""
                it[Constants.KEY_TIME_STAMP] = timeStamp
                it[Constants.KEY_EMAIL]= auth.currentUser?.email!!
                it[Constants.KEY_IMAGEPATH]=imagePath
            }

            secondPersonReference.add(messageObj)

            localDb.addMessage(receiverId, Message(auth.currentUser?.uid!!,"",timeStamp,
                auth.currentUser?.email!!,imagePath
            ))

            messageAdapter.addMessage(Message(auth.currentUser?.uid!!,"",timeStamp,
                auth.currentUser?.email!!,imagePath
            ))

//            storageReference.downloadUrl.addOnSuccessListener {
//
//              //  userReference.document(userId).update("profilePhoto",it.toString())
//
        //            }

        }

    }

}


