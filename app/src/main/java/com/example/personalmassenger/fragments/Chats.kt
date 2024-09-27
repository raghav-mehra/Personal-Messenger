package com.example.personalmassenger.fragments

import LLD.ChatsManager.ChatsFacade
import Utils.Constants
import Utils.FirebaseUtil
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalmassenger.MainToolbar
import com.example.personalmassenger.MessagingActivity
import com.example.personalmassenger.R
import com.example.personalmassenger.adapters.ChatsRecyclerViewAdapter
import com.example.personalmassenger.localDatabse.localDbHandler
import com.example.personalmassenger.viewModel.ToolbarViewModel
import com.google.firebase.firestore.ListenerRegistration


class Chats : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    lateinit var chatRecyclerAdapter: ChatsRecyclerViewAdapter
    private lateinit var registration: ListenerRegistration
    private lateinit var mainToolbar: MainToolbar
    private var chatsFacade=ChatsFacade(this)
    private val toolbarViewModel=ToolbarViewModel()

    override fun onResume() {
        registration=FirebaseUtil.currentUserChats().orderBy(Constants.KEY_TIME_STAMP).addSnapshotListener { snapshot, error ->
            error.let {
                Log.d("Error",it.toString())
            }
            snapshot.let {docs->
                chatsFacade.receiveMessages(docs)
            }
        }
        super.onResume()
    }

    override fun onStart() {
        chatRecyclerAdapter.refresh()
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_chats, container, false)
        chatRecyclerAdapter=ChatsRecyclerViewAdapter(activity?.applicationContext!!)
        chatRecyclerView= view.findViewById(R.id.chats_recyclerview)
        chatRecyclerView.adapter=chatRecyclerAdapter
        chatRecyclerView.layoutManager=LinearLayoutManager(context)
        mainToolbar= activity as MainToolbar
        chatRecyclerAdapter.onItemClick={
            Log.d("chatINfoEmail",it.email)
            FirebaseUtil.userDetails(it.email).get().addOnSuccessListener {doc->
                val bundle = Bundle()
                bundle.putString(Constants.KEY_ID, doc.getString(Constants.KEY_UID).toString())
                bundle.putString(Constants.KEY_USERNAME, it.name)
                bundle.putString(Constants.KEY_EMAIL, it.email)
                val intent= Intent(activity, MessagingActivity::class.java)
                intent.putExtra(Constants.KEY_REFERENCE,bundle)
                startActivity(intent)
            }
        }
        chatRecyclerAdapter.onLongPressItemClick={chatInfo,selectedItems,adapterRef ->
            if (selectedItems!=0){
                mainToolbar.itemSelected()
                mainToolbar.deleteSelectedItems(adapterRef)
            }
            else{
                mainToolbar.itemNotSelected()
            }

         //   toolbarViewModel.showOptionsForSelectedItems()
        }
        Log.d("onCreate","called")
        return view
    }

    override fun onStop() {
        registration.remove()
        super.onStop()
    }
}