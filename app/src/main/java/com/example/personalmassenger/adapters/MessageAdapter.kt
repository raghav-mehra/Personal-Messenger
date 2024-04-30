package com.example.personalmassenger.adapters


import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalmassenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import model.Message


class MessageAdapter():RecyclerView.Adapter<MessageAdapter.viewHolder>() {
    private var imagesReference=FirebaseStorage.getInstance().reference
    private var list= mutableListOf<Message>()
    val left=0
    val notify=2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
       if (viewType==left){
           return viewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_reciever_recycler,parent,false))
       }
        if (viewType==notify){
            return viewHolder(LayoutInflater.from(parent.context).inflate(R.layout.system_notify_recycler,parent,false))
        }
           return viewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_sender_recycler,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
         if(list[position].senderId== FirebaseAuth.getInstance().currentUser!!.uid)
             return 1
        return 0
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.textviewMessage.text=list[position].message
        holder.textviewTime.text=list[position].timeStamp
      //  holder.title_name.text="~ ${list[position].email}"
        val imagePath=list[position].imagePath
        if (imagePath!="null"){
            imagesReference.child(imagePath).getBytes(2048*2048).addOnSuccessListener {
                holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(it,0,it.size))
            }
        }

    }

    fun addMessage(value:Message){
        list.add(value)
        notifyDataSetChanged()
    }

    fun updateMessages(messageList:MutableList<Message>){
        list=messageList
        notifyDataSetChanged()
    }
    fun getLastPosition():Int{
        return list.size-1
    }
    inner class viewHolder(view:View):RecyclerView.ViewHolder(view){
        val textviewMessage:TextView=view.findViewById(R.id.message_textview)
        val textviewTime:TextView=view.findViewById(R.id.message_time_textview)
        val imageView:ImageView=view.findViewById(R.id.image_sender_imageView)
//        val title_name:TextView=view.findViewById(R.id.Name_title)
    }

}