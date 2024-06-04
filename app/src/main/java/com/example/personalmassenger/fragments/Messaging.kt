package com.example.personalmassenger.fragments

import Utils.Constants
import Utils.FirebaseUtil
import android.annotation.SuppressLint
import android.content.DialogInterface
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AlertDialogLayout
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
import com.mikhaellopez.circularimageview.CircularImageView
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
    private lateinit var toolbar_backButton:ImageView
    private lateinit var toolbar_profile_pic:CircularImageView
    private val currentUserId:String=FirebaseUtil.currentUserId()
    private val currentUserEmail:String=FirebaseUtil.currentUserEmail()
    private lateinit var localDb:localDbHandler
    private lateinit var receiverId:String
    private lateinit var receiverEmail:String
    private lateinit var userTableName:String
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
        receiverEmail=arguments?.getString(Constants.KEY_EMAIL).toString()
        receiverId = arguments?.getString(Constants.KEY_ID).toString()
        val view = inflater.inflate(R.layout.fragment_user_chat, container, false)
        userTableName=Constants.getTableName(receiverEmail)
        val messageList=localDbHandler(activity?.applicationContext!!).getFullChat(userTableName)
        recyclerview = view.findViewById(R.id.message_recycler)
        localDb=localDbHandler(activity?.applicationContext!!)
        messageEditText = view.findViewById(R.id.fragment_message_edit_text)
        messageSendButton = view.findViewById(R.id.message_send_button)
        messageAdapter = MessageAdapter()
        toolbar_profile_pic=view.findViewById(R.id.toolbar_profile_pic)
        toolbar=view.findViewById(R.id.messaging_toolbar)
        toolbar_title=view.findViewById(R.id.toolbar_user_name)
        toolbar_title.text=arguments?.getString(Constants.KEY_USERNAME)
        toolbar_backButton=view.findViewById(R.id.toolbar_back)
        recyclerview.adapter = messageAdapter
        val manager=LinearLayoutManager(activity)
        recyclerview.layoutManager = manager
        messageAdapter.updateMessages(messageList)
        messageAdapter.registerAdapterDataObserver(object:AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerview.smoothScrollToPosition(itemCount-1)
            }
        })
        FirebaseUtil.profilePicReference(receiverEmail).getBytes(1024*1024).addOnSuccessListener {
            toolbar_profile_pic.setImageBitmap(BitmapFactory.decodeByteArray(it,0,it.size))
        }
        toolbar_backButton.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.action_clearchat->{
                    alertDialog()
                    true
                }

                else -> {
                    true
                }
            }
        }
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

        FirebaseUtil.currentUserChats().whereEqualTo(Constants.KEY_SENDER_ID,receiverId).orderBy(Constants.KEY_TIME_STAMP).addSnapshotListener { snapshot, error ->
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
                    localDb.addMessage(userTableName,Message(
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
                    FirebaseUtil.currentUserChats().document(dc.document.id).delete()
                }
            }
        }
        return view
    }

    private fun sendMessage() {
        val message = messageEditText.text.toString().trim()
        Log.d("receiverId",receiverId)
        if (message.isEmpty()) {
            messageEditText.error = "Enter some message to send"
        } else {
            val calender = Calendar.getInstance()
            val hour = calender.get(Calendar.HOUR_OF_DAY)
            val minute = calender.get(Calendar.MINUTE)
            val timeStamp = "${calender.get(Calendar.DATE)} $hour:$minute"
            val messageObj = mutableMapOf<String, String>().also {
                it[Constants.KEY_SENDER_ID] = currentUserId
                it[Constants.KEY_MESSAGE] = message
                it[Constants.KEY_TIME_STAMP] = timeStamp
                it[Constants.KEY_EMAIL]= currentUserEmail
                it[Constants.KEY_IMAGEPATH]="null"
            }
            FirebaseUtil.userChats(receiverId).add(messageObj).addOnSuccessListener {
                val senderMessage= Message(
                    messageObj[Constants.KEY_SENDER_ID].toString(),
                    messageObj[Constants.KEY_MESSAGE].toString(),
                    messageObj[Constants.KEY_TIME_STAMP].toString(),
                    messageObj[Constants.KEY_EMAIL].toString(),
                    messageObj[Constants.KEY_IMAGEPATH].toString()
                )
                localDb.addMessage(userTableName,senderMessage)
                messageAdapter.addMessage(senderMessage)
                recyclerview.smoothScrollToPosition(messageAdapter.getLastPosition())
                messageEditText.text.clear()

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
        val userId=FirebaseUtil.currentUserId()
//        val mediaNum=db.collection("Chats").document(receiverId)
//            .collection("MediaNumber").get().addOnSuccessListener {
//                it.documents.get()
//            }
        val imagePath="chat_images/$userId/${System.currentTimeMillis()}"
        FirebaseUtil.getStorageReference().child(imagePath).putFile(uri!!).addOnSuccessListener {
            val calender = Calendar.getInstance()
            val hour = calender.get(Calendar.HOUR_OF_DAY)
            val minute = calender.get(Calendar.MINUTE)
            val timeStamp = "$hour:$minute"
            val messageObj = mutableMapOf<String, String>().also {
                it[Constants.KEY_SENDER_ID] =currentUserId
                it[Constants.KEY_MESSAGE] = ""
                it[Constants.KEY_TIME_STAMP] = timeStamp
                it[Constants.KEY_EMAIL]= currentUserEmail
                it[Constants.KEY_IMAGEPATH]=imagePath
            }
            FirebaseUtil.userChats(receiverId).add(messageObj)
            localDb.addMessage(userTableName, Message(currentUserId,"",timeStamp,
                currentUserEmail,imagePath
            ))

            messageAdapter.addMessage(Message(currentUserId,"",timeStamp,
                currentUserEmail,imagePath
            ))

//            storageReference.downloadUrl.addOnSuccessListener {
//
//              //  userReference.document(userId).update("profilePhoto",it.toString())
//
        //            }

        }

    }
    private fun alertDialog(){
        val alertDialog= AlertDialog.Builder(view?.context!!)
        alertDialog.setTitle("Confirm Deletion")
        .setMessage("All chat with this user will be deleted. This action cannot be undone. Delete chat?")
        .setIcon(R.drawable.warning_svgrepo_com)
        .setPositiveButton("Yes"){dialog,_->
                localDb.deleteTableRecord(userTableName)
                messageAdapter.clearChat()
                dialog.dismiss()
        }
       .setNegativeButton("No"){dialog,_->
           dialog.dismiss()
        }.create().show()
    }
}


