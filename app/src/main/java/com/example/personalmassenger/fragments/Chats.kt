package com.example.personalmassenger.fragments

import Utils.Constants
import Utils.FirebaseUtil
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalmassenger.Communicator
import com.example.personalmassenger.MainToolbar
import com.example.personalmassenger.R
import com.example.personalmassenger.adapters.ChatsRecyclerViewAdapter
import com.example.personalmassenger.localDatabse.localDbHandler
import com.example.personalmassenger.viewModel.NewViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import model.ChatInfo
import model.Message


class Chats : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatRecyclerAdapter: ChatsRecyclerViewAdapter
    private lateinit var communicator: Communicator
    private lateinit var mainToolbar: MainToolbar

    override fun onResume() {
        chatRecyclerAdapter.refresh()
        super.onResume()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_chats, container, false)
        communicator=activity as Communicator
        mainToolbar=activity as MainToolbar
        chatRecyclerAdapter=ChatsRecyclerViewAdapter(activity?.applicationContext!!)
        chatRecyclerView= view.findViewById(R.id.chats_recyclerview)
        chatRecyclerView.adapter=chatRecyclerAdapter
        chatRecyclerView.layoutManager=LinearLayoutManager(context)
        communicator.passChatsAdapterReference(chatRecyclerAdapter)

        chatRecyclerAdapter.onItemClick={
            Log.d("chatINfoEmail",it.email)
            FirebaseUtil.userDetails(it.email).get().addOnSuccessListener {doc->
                communicator.passData(doc.getString(Constants.KEY_UID).toString(),it.name,it.email)
            }
        }
        chatRecyclerAdapter.onLongPressItemClick={
            mainToolbar.itemSelected()
        }
        Log.d("onCreate","called")
        FirebaseUtil.currentUserChats().orderBy(Constants.KEY_TIME_STAMP).addSnapshotListener { snapshot, error ->
            error.let {
                Log.d("Error",it.toString())
            }

            snapshot.let {docs->
                val hash=HashMap<String,ChatInfo>()
                for (doc in docs?.documentChanges!!){
                    val message=doc.document.getString(Constants.KEY_MESSAGE).toString()
                    val id=doc.document.getString(Constants.KEY_SENDER_ID).toString()
                    val email=doc.document.getString(Constants.KEY_EMAIL).toString()
                    val timeStamp=doc.document.getString(Constants.KEY_TIME_STAMP).toString()
                    Log.d("senderId",id)
                    hash[id]=ChatInfo(email,"",Pair(message,timeStamp),email,id)

                }
                for((key,value) in hash){
                    chatRecyclerAdapter.updateChatList(key,value)
                }
            }
        }
        return view

    }





}