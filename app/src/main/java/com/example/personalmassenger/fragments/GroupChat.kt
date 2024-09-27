package com.example.personalmassenger.fragments

import Utils.Constants
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalmassenger.R
import com.example.personalmassenger.adapters.MessageAdapter
import com.example.personalmassenger.localDatabse.localDbHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import model.Message

class GroupChat : Fragment() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageEditText: EditText
    private lateinit var messageSendButton: FloatingActionButton
    private lateinit var toolbar: Toolbar
    private lateinit var db: FirebaseFirestore
    private  var storageReference: StorageReference = FirebaseStorage.getInstance().reference
    private lateinit var GroupReference: CollectionReference
    private lateinit var myReference: CollectionReference
    private lateinit var localDb: localDbHandler
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_group_chat, container, false)
        val group_name=arguments?.getString(Constants.KEY_NAME)
        val admin_id=arguments?.getString(Constants.KEY_ID)
        val doc_name="$admin_id"+"$group_name"
        toolbar=view.findViewById(R.id.Group_chat_toolbar)
        toolbar.setTitle(group_name)
        messageAdapter = MessageAdapter()
        recyclerview.adapter = messageAdapter
        recyclerview.layoutManager = LinearLayoutManager(activity)
        GroupReference=db.collection("Group Chat").document(doc_name).collection("Messages")
        GroupReference.addSnapshotListener {snapshot, error ->
            error?.let {
                Toast.makeText(activity,it.message, Toast.LENGTH_SHORT).show()
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
//                    localDb.addMessage(doc_name, Message(
//                        receivedEmail.toString(),
//                        receivedMessage.toString(),
//                        receivedTime.toString(),receivedEmail.toString(),receivedImagePath
//                    )
//                    )
//                    messageAdapter.addMessage(Message(
//                        receivedEmail.toString(),
//                        receivedMessage.toString(),
//                        receivedTime.toString(),receivedEmail.toString(),receivedImagePath
//                    ))

            }
            }

        }

        return view
    }

}