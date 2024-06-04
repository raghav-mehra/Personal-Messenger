package com.example.personalmassenger.adapters

import Utils.Constants
import Utils.FirebaseUtil
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.personalmassenger.R
import com.example.personalmassenger.localDatabse.localDbHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import model.ChatInfo
import java.io.File
import kotlin.math.max

class ChatsRecyclerViewAdapter(context:Context): RecyclerView.Adapter<ChatsRecyclerViewAdapter.ChatsViewHolder>() {
    private val myEmail=FirebaseUtil.currentUserEmail()
    private var localDb= localDbHandler(context)
    private var chatList: MutableList<ChatInfo> =localDb.loadChatInfo()
    private var map= mutableMapOf<String,Int>()
    private var selectedItems=mutableSetOf<Int>()
    var onItemClick:((ChatInfo)->Unit)?=null
    var onLongPressItemClick:(()->Unit)?=null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        if(viewType==0) {
            return ChatsViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.chats_recyclerview_item, parent, false)
            )
        }
        return ChatsViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.empty_fragment_message, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return max(chatList.size,1)
    }

    override fun getItemViewType(position: Int): Int {
        return if(chatList.size>0) 0 else 1
    }
    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        if(chatList.size!=0) {
            val chat = chatList[position]
            map[chat.email] = position
            holder.name.text = chat.name
            holder.time.text = chat.lastMessage.second
            holder.itemView.setOnClickListener {
                if (selectedItems.size == 0) {
                    onItemClick?.invoke(chat)
                } else if (selectedItems.contains(position)) {
                    holder.unMarkItem()
                    selectedItems.remove(position)
                    if (selectedItems.isEmpty()) {
                        onLongPressItemClick?.invoke()
                    }
                } else {
                    holder.markItem()
                    selectedItems.add(position)
                }
            }
            holder.itemView.setOnLongClickListener {
                if (selectedItems.size == 0) {
                    holder.markItem()
                    selectedItems.add(position)
                    onLongPressItemClick?.invoke()
                }

                true
            }

            FirebaseUtil.profilePicReference(chat.email).getBytes(1024 * 1024)
                .addOnSuccessListener {
                    holder.profilePic.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
                }

            if (chat.messageEmail == myEmail) {
                holder.lastMessage.text = if(chat.lastMessage.first.isNotEmpty()) "You: ${chat.lastMessage.first}" else "You:Document"
            } else {
                holder.lastMessage.text = chat.lastMessage.first
            }
        }
    //  holder.profilePic.setImageURI(chat.profilePhoto)
    }

    inner class ChatsViewHolder(view:View):RecyclerView.ViewHolder(view){
        var name=view.findViewById<TextView>(R.id.chat_person_name)
        val profilePic=view.findViewById<ImageView>(R.id.chat_person_profilePhoto)
        val time=view.findViewById<TextView>(R.id.chat_last_message_time)
        val lastMessage=view.findViewById<TextView>(R.id.chat_last_message)
        val tickMark=view.findViewById<ImageView>(R.id.marked_imageView)
        val bg=view.findViewById<CardView>(R.id.item_background)
    //    val emptyFragTv=view.findViewById<TextView>(R.id.empty_fragment_textView)
        fun markItem(){
            tickMark.visibility=View.VISIBLE
            bg.setBackgroundResource(R.drawable.selected_item_background)
        }
        fun unMarkItem(){
            tickMark.visibility=View.INVISIBLE
            bg.setBackgroundResource(R.drawable.not_selected_item_background)
        }
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
    fun deleteSelectedChats(){
        for( pos in selectedItems){
            localDb.deleteTableRecord(Constants.getTableName(chatList[pos].email))
            chatList.removeAt(pos)
        }
        notifyDataSetChanged()
    }
    fun deselectAll(){

    }

}