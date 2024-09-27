package com.example.personalmassenger

import LLD.MessagingManager.MessageFacade.MessageFacade
import LLD.MessagingManager.MessageProxy.MessageProxy
import Utils.Constants
import Utils.FirebaseUtil
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalmassenger.adapters.MessageAdapter
import com.example.personalmassenger.localDatabse.localDbHandler
import com.example.personalmassenger.viewModel.MessagingViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.mikhaellopez.circularimageview.CircularImageView
import model.Message
import java.util.Calendar

class MessagingActivity : AppCompatActivity() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageEditText: EditText
    private lateinit var messageSendButton: FloatingActionButton
    private lateinit var toolbar_title: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var toolbar_backButton: ImageView
    private lateinit var toolbar_profile_pic: CircularImageView
    private val currentUserId:String= FirebaseUtil.currentUserId()
    private val currentUserEmail:String= FirebaseUtil.currentUserEmail()
    private var localDb=localDbHandler.getInstance(this)
    private lateinit var receiverId:String
    private lateinit var referenceData:Bundle
    private lateinit var receiverEmail:String
    private lateinit var userTableName:String
    private lateinit var  messageViewModel: MessagingViewModel
    private lateinit var messageFacade: MessageProxy
    private val register=registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
        if (it!=null){
            uploadImage(it)
        }
    }

    init {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
          setContentView(R.layout.activity_messaging)
          referenceData= intent.extras?.getBundle(Constants.KEY_REFERENCE)!!
          receiverEmail=referenceData.getString(Constants.KEY_EMAIL).toString()
          receiverId=referenceData.getString(Constants.KEY_ID).toString()
          userTableName= Constants.getTableName(receiverEmail)
        messageViewModel= MessagingViewModel(this,userTableName)
        messageFacade=MessageProxy (messageViewModel,localDb)
          recyclerview =findViewById(R.id.message_recycler)
          messageEditText = findViewById(R.id.fragment_message_edit_text)
          messageSendButton = findViewById(R.id.message_send_button)
          messageAdapter = MessageAdapter()
          toolbar_profile_pic=findViewById(R.id.toolbar_profile_pic)
          toolbar=findViewById(R.id.messaging_toolbar)
          toolbar_title=findViewById(R.id.toolbar_user_name)
          toolbar_title.text=referenceData?.getString(Constants.KEY_USERNAME )
          toolbar_backButton=findViewById(R.id.toolbar_back)
          recyclerview.adapter = messageAdapter
          val manager= LinearLayoutManager(this)
          recyclerview.layoutManager = manager
        messageViewModel.messageList.observe(this, Observer {
             messagesList->
             messageAdapter.updateMessages(messagesList)
            Log.d("message",messagesList.toString())
           // recyclerview.smoothScrollToPosition(messageAdapter.getLastPosition())
        })

        messageAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
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
                    val popupMenu= PopupMenu(this,messageEditText)
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

        FirebaseUtil.currentUserChats().whereEqualTo(Constants.KEY_SENDER_EMAIL,receiverEmail).orderBy(
            Constants.KEY_TIME_STAMP).addSnapshotListener(this,) { snapshot, error ->
            error?.let {
                Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
                Log.d("snapshot error: ",it.message.toString())
            }
            snapshot?.let {
                messageFacade.receive(it)
            }
        }
    }
    private fun sendMessage() {
        val message = messageEditText.text.toString().trim()
        if (message.isEmpty()) {
            messageEditText.error = "Enter some message to send"
        } else {
            val calender = Calendar.getInstance()
            val hour = calender.get(Calendar.HOUR_OF_DAY)
            val minute = calender.get(Calendar.MINUTE)
            val timeStamp = "${calender.get(Calendar.DATE)} $hour:$minute"
            val messageObj = Message("Text",message,timeStamp,currentUserEmail,receiverEmail,"null")
            messageFacade.send(messageObj)
            messageEditText.text.clear()

        }
    }
    private fun uploadImage(uri: Uri?){
        val receiverId =referenceData.getString(Constants.KEY_ID).toString()
        val userId=FirebaseUtil.currentUserId()
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
//            localDb.addMessage(userTableName, Message(currentUserId,"",timeStamp,
//                currentUserEmail,imagePath
//            ))
//
//            messageAdapter.addMessage(Message(currentUserId,"",timeStamp,
//                currentUserEmail,imagePath
//            ))

//            storageReference.downloadUrl.addOnSuccessListener {
//
//              //  userReference.document(userId).update("profilePhoto",it.toString())
//
            //            }

        }

    }
    private fun alertDialog(){
        val alertDialog= AlertDialog.Builder(this)
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