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
import android.widget.RelativeLayout
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
    var localDb= localDbHandler.getInstance(context)
    private var chatList: MutableList<ChatInfo> = localDb.loadChatInfo()
    private var map= mutableMapOf<String,Int>()
    private var selectedItems=0
    var onItemClick:((ChatInfo)->Unit)?=null
    var onLongPressItemClick:((ChatInfo,Int,ChatsRecyclerViewAdapter)->Unit)?=null

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
         //   map[chat.email] = position
            holder.name.text = chat.name
            holder.time.text = chat.lastMessage.second
            holder.profilePic.setImageResource(R.drawable.ic_profile)
            if (chat.isSelected) {
                holder.bg.setBackgroundResource(R.drawable.selected_item_background)
            }
            if (chat.unreadMessages!=0){
                holder.unreadMessagesTv.text=if(chat.unreadMessages > 99) "99+" else chat.unreadMessages.toString()
                holder.unreadMessageBg.visibility=View.VISIBLE
            }
            else{
                holder.unreadMessageBg.visibility=View.INVISIBLE
            }
            holder.itemView.setOnClickListener {
                if (selectedItems == 0) {
                    onItemClick?.invoke(chat)
                }
                else if (chatList[position].isSelected) {
                    holder.unMarkItem()
                    chatList[position].isSelected=false
                    selectedItems--
                    if (selectedItems==0) {
                        onLongPressItemClick?.invoke(chat,selectedItems,this)
                    }
                } else {
                    holder.markItem()
                    chatList[position].isSelected=true
                    selectedItems++
                }
            }
            holder.itemView.setOnLongClickListener {
                if (selectedItems == 0) {
//                    holder.markItem()
                    chatList[position].isSelected=true
                    selectedItems++
                    notifyDataSetChanged()
                    onLongPressItemClick?.invoke(chat,selectedItems,this)
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
        val unreadMessageBg=view.findViewById<RelativeLayout>(R.id.unread_messages_RL)
        val unreadMessagesTv=view.findViewById<TextView>(R.id.unread_messages_tv)
    //    val emptyFragTv=view.findViewById<TextView>(R.id.empty_fragment_textView)
        fun markItem(){
           // tickMark.visibility=View.VISIBLE
            bg.setBackgroundResource(R.drawable.selected_item_background)
        }
        fun unMarkItem(){
           // tickMark.visibility=View.INVISIBLE
            bg.setBackgroundResource(R.drawable.not_selected_item_background)
        }

    }

    fun updateChatList(list:MutableList<ChatInfo>){
        chatList=list
      //  chatList.sortWith(compareByDescending ({it.lastMessage.second}))
        Log.d("chatInfoUpdate",chatList.toString())
        notifyDataSetChanged()
    }

    fun refresh(){
        updateChatList(localDb.loadChatInfo())
    }

    fun insertAtTop(value:ChatInfo) {
        if (!map.containsKey(value.email)) {
            chatList.add(0, value)

        }
        else {
            value.isSelected=chatList[map[value.email]!!].isSelected
            chatList.removeAt(map[value.email]!!)
            chatList.add(0, value)
        }
        notifyDataSetChanged()
    }

    fun deleteSelectedChats(){
        val iterator=chatList.listIterator()
        with (iterator) {
            forEach {item->
                if (item.isSelected) {
                    localDb.deleteTableRecord(Constants.getTableName(item.email))
                  //  chatList.remove(item)
                }
            }
        }
        chatList.removeAll{it.isSelected==true }
        notifyDataSetChanged()
    }

    fun deselectAll()
    {

    }


}