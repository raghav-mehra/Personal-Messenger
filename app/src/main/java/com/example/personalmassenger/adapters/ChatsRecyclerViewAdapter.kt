package com.example.personalmassenger.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.personalmassenger.R
import com.example.personalmassenger.localDatabse.localDbHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import model.ChatInfo
import java.io.File

class ChatsRecyclerViewAdapter(context:Context): RecyclerView.Adapter<ChatsRecyclerViewAdapter.ChatsViewHolder>() {
    private var localDb= localDbHandler(context)
    private var chatList: MutableList<ChatInfo> =localDb.loadChatInfo()
    private var map= mutableMapOf<String,Int>()
    private var storageReference: StorageReference= FirebaseStorage.getInstance().reference
    var onItemClick:((ChatInfo)->Unit)?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chats_recyclerview_item,parent,false))
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
       val chat=chatList[position]
        map[chat.uid]=position
        holder.name.text=chat.name
        holder.time.text=chat.lastMessage.second
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(chatList[position])
        }
        storageReference.child("profile_photos/${chat.uid}").getBytes(1024*1024).addOnSuccessListener {
            holder.profilePic.setImageBitmap(BitmapFactory.decodeByteArray(it,0,it.size))
        }

        if(chat.email==FirebaseAuth.getInstance().currentUser?.email){
            holder.lastMessage.text= "You: ${chat.lastMessage.first}"
        }
        else{
            holder.lastMessage.text= chat.lastMessage.first
        }

    //  holder.profilePic.setImageURI(chat.profilePhoto)
    }

    inner class ChatsViewHolder(view:View):RecyclerView.ViewHolder(view){
        var name=view.findViewById<TextView>(R.id.chat_person_name)
        val profilePic=view.findViewById<ImageView>(R.id.chat_person_profilePhoto)
        val time=view.findViewById<TextView>(R.id.chat_last_message_time)
        val lastMessage=view.findViewById<TextView>(R.id.chat_last_message)
    }

    fun updateChatList(list:MutableList<ChatInfo>){
        chatList=list
        chatList.sortWith(compareByDescending ({it.lastMessage.second}))
        Log.d("chatInfoUpdate",chatList.toString())
        notifyDataSetChanged()
    }
    fun refresh(){
        updateChatList(localDb.loadChatInfo())

    }
    fun updateChatList(id:String,value:ChatInfo) {
        if (!map.containsKey(id)) {
            chatList.add(0, value)

        } else {
            chatList.removeAt(map[id]!!)
            chatList.add(0, value)
        }
        notifyDataSetChanged()
    }

}