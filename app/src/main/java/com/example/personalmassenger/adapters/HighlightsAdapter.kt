package com.example.personalmassenger.adapters

import Utils.Constants
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.circularstatusview.CircularStatusView
import com.example.personalmassenger.Communicator
import com.example.personalmassenger.R
import com.google.firebase.firestore.FirebaseFirestore
import model.Contact
import model.Highlight
import java.util.concurrent.TimeUnit

class HighlightsAdapter:RecyclerView.Adapter<HighlightsAdapter.HighlightsViewHolder>() {
     var highlightList= mutableListOf<Highlight>()
    var onItemClick: ((Highlight) ->Unit)?=null
    private var userReference= FirebaseFirestore.getInstance().collection("Users")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightsViewHolder {
        return HighlightsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.highlights_recycler_item,parent,false))
    }

    override fun getItemCount(): Int {
        return highlightList.size
    }

    override fun onBindViewHolder(holder: HighlightsViewHolder, position: Int) {
        val highlight=highlightList[position]
        holder.circularStatusView.setPortionsCount(highlight.portionsCount)
        holder.name.text=highlight.name
       holder.itemView.setOnClickListener {
           onItemClick?.invoke(highlight)
       }
    }
    inner class HighlightsViewHolder(view: View):RecyclerView.ViewHolder(view){
        val name=view.findViewById<TextView>(R.id.highlights_person_name)
        val profilePic=view.findViewById<ImageView>(R.id.highlights_person_profilePhoto)
        val circularStatusView=view.findViewById<CircularStatusView>(R.id.circular_status_view)
        val time=view.findViewById<TextView>(R.id.highlights_time)
//        val lastMessage=view.findViewById<TextView>(R.id.chat_last_message)
    }
    fun update(list:MutableList<Highlight>){
        highlightList=list
        notifyDataSetChanged()
    }
    fun update(element:Highlight){
        highlightList.add(element)
        notifyDataSetChanged()
    }

}