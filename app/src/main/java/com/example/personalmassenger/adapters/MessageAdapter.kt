package com.example.personalmassenger.adapters


import Utils.Constants
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


class MessageAdapter():RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var imagesReference=FirebaseStorage.getInstance().reference
    private var list= mutableListOf<Message>()
    //private var notifyReminders=1
    val left=0
    val notify=2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       if (viewType==left){
           return viewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_reciever_recycler,parent,false))
       }
        if (viewType==notify){
            return viewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.system_notify_recycler,parent,false))
        }
           return viewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_sender_recycler,parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        if(list[position].type=="notify"){
            return 2
        }
        else if(list[position].senderEmail== FirebaseAuth.getInstance().currentUser!!.email)
             return 1
        return 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

       if(holder.itemViewType==notify){
            (holder as viewHolder2).notifyTextView.text=Constants.compareDay(list[position].timeStamp)
        }
        else {
            (holder as viewHolder).bind(position)
        }
    }

    fun addMessage(value:Message){
        list.add(value)
        notifyDataSetChanged()
    }

    fun updateMessages(messageList:MutableList<Message>){
        list=messageList
        if(list.size > 0 && list[0].type!="notify") {
            list.add(0, Message("notify", "", list[0].timeStamp, "", "", "null"))
        }
        var i=0
        while (i<list.size){
            if (i>0 && Constants.isOtherDay(list[i-1].timeStamp,list[i].timeStamp) && list[i-1].type!="notify"){
                list.add(i,Message("notify","",list[i].timeStamp,"","","null"))
            }
            i++
        }
        notifyDataSetChanged()
    }
    fun getLastPosition():Int{
        return list.size-1
    }
    fun clearChat(){
        list.clear()
        notifyDataSetChanged()
    }
    inner class viewHolder(view:View):RecyclerView.ViewHolder(view){
        val textviewMessage:TextView=view.findViewById(R.id.message_textview)
        val textviewTime:TextView=view.findViewById(R.id.message_time_textview)
        val imageView:ImageView=view.findViewById(R.id.image_sender_imageView)
        fun bind(position: Int){
            textviewMessage.text = list[position].text
            textviewTime.text = Constants.getFormattedTime(list[position].timeStamp)
            //  holder.title_name.text="~ ${list[position].email}"

            val imagePath = list[position].fileReference
            if (imagePath != "null") {
                imagesReference.child(imagePath).getBytes(2048 * 2048).addOnSuccessListener {
                    imageView.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
                }
            }
        }

//        val title_name:TextView=view.findViewById(R.id.Name_title)
    }
    inner class viewHolder2(view:View):RecyclerView.ViewHolder(view){
        val notifyTextView:TextView=view.findViewById(R.id.notify_title_textview)
    }

}