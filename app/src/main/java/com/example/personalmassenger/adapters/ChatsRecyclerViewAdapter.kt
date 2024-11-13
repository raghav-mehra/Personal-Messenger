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
    private lateinit var chatList: MutableList<ChatInfo>
    private var map= mutableMapOf<String,ChatInfo>()
    var selectedItems=0
    var onItemClick:((ChatInfo)->Unit)?=null
    var hideMenuOptions:(()->Unit)?=null
    var showMenuOptions:(()->Unit)?=null
    var onLongPressItemClick:((ChatInfo,Int,ChatsRecyclerViewAdapter)->Unit)?=null
    init {
        refresh()
    }
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
    fun refreshUi(){
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        if(chatList.size!=0) {
            val chat = chatList[position]
         //   map[chat.email] = position
            holder.name.text = chat.name
            holder.time.text = Constants.getFormattedTime(chat.lastMessage.second)
            holder.profilePic.setImageResource(R.drawable.ic_profile)
            if (chat.isSelected) {
                holder.bg.setBackgroundResource(R.drawable.selected_item_background)
            }
            else{
                holder.unMarkItem()
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
                    chatList[position].unreadMessages=0
                }
                else if (chatList[position].isSelected) {
                    holder.unMarkItem()
                    chatList[position].isSelected=false
                    selectedItems--
                    if (selectedItems==0) {
                        hideMenuOptions?.invoke()
                     //   onLongPressItemClick?.invoke(chat,selectedItems,this)
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
                    if(chatList.size==1){
                        chatList[0].isSelected=true
                    }
                    else {
                        chatList[position].isSelected = true
                    }
                    selectedItems++
                    notifyItemChanged(position)
                    showMenuOptions?.invoke()
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
        map.clear()
        for (i in list){
            map.put(i.email,i)
        }
        chatList.sortWith(compareByDescending ({it.lastMessage.second}))
        Log.d("chatInfoUpdate",chatList.toString())
        notifyDataSetChanged()
    }

    fun refresh(){
        updateChatList(localDb.loadChatInfo())
    }

    fun insertAtTop(value:ChatInfo) {
        if (!map.containsKey(value.email)) {
            chatList.add(0, value)
            map.put(value.email,value)
          //  notifyDataSetChanged()
            if(chatList.size==1){
                notifyDataSetChanged()
            }
            notifyItemRangeInserted(0,chatList.size-1)
//            try {
//                notifyItemInserted(0)
//            }
//            catch (e:Exception){
//                notifyDataSetChanged()
//            }
        }
        else {
//            chatList.removeAll{it.email==value.email}

            map.replace(value.email,value)
            var ind=0
            for (i in chatList){
                if (i.email==value.email){
                    chatList[ind].unreadMessages += value.unreadMessages
                    chatList[ind].lastMessage = value.lastMessage
                    chatList.add(0,chatList[ind])
                    chatList.removeAt(ind+1)
                    notifyItemRemoved(ind)
                    notifyItemInserted(0)
                    break
                }
                ind++
            }


        }

    }

    fun deleteSelectedChats(){
        val iterator=chatList.listIterator()
        with (iterator) {
            forEach {item->
                if (item.isSelected) {
                    localDb.deleteTableRecord(Constants.getTableName(item.email))
                    notifyItemRemoved(iterator.nextIndex()-1)
                  //  chatList.remove(item)
                }
            }
        }
        chatList.removeAll{it.isSelected==true }
        map.clear()
        selectedItems=0
    //    notifyDataSetChanged()
    }

    fun deselectAll()
    {
        hideMenuOptions?.invoke()
        selectedItems=0
        val iterator=chatList.listIterator()
        with (iterator) {
            forEach {item->
                if (item.isSelected) {
                    chatList[iterator.nextIndex()-1].isSelected=false
                }
            }
        }
        notifyDataSetChanged()

    }


}