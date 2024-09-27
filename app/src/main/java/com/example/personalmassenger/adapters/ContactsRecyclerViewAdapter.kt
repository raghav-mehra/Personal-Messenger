package com.example.personalmassenger.adapters

import Utils.FirebaseUtil
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.personalmassenger.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import model.Contact

class ContactsRecyclerViewAdapter(): RecyclerView.Adapter<ContactsRecyclerViewAdapter.ContactsViewHolder>() {

    private var contactList = mutableListOf<Contact>()
    var onItemClick: ((Contact) ->Unit)?=null
    var onLongClickItem: ((Contact) ->Unit)?=null
    var deleteContact:((Contact)->Unit)?=null
    var editContact:((Contact)->Unit)?=null
    private var selectedItems= mutableSetOf<Int>()
    private var storageReference:StorageReference=FirebaseStorage.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        return ContactsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.contacts_recyclerview_item,parent,false))
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val contact=contactList[position]
        val popupMenu= PopupMenu(holder.context,holder.optionsButton)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_contacts,popupMenu.menu)
        holder.name.text=contact.userName
        holder.email.text=contact.email
        holder.optionsButton.setOnClickListener {
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.action_Delete->{
                        contactList.removeAt(position)
                        deleteContact?.invoke(contact)
                        updateContactList()
                    }
                    R.id.action_Edit->{
                        editContact?.invoke(contact)
                        contactList.removeAt(position)
                    }
                }
                true
            })
          //  popupMenu.setForceShowIcon(true)
            popupMenu.show()
        }
        holder.itemView.setOnClickListener {
            if (selectedItems.contains(position)){
                holder.unMarkItem()
                selectedItems.remove(position)
            }
            else if(selectedItems.isNotEmpty()){
                holder.markItem()
                selectedItems.add(position)
            }
            else{
                onItemClick?.invoke(contactList[position])
            }
        }
        holder.itemView.setOnLongClickListener {
            holder.markItem()
            selectedItems.add(position)
            true
        }

        FirebaseUtil.profilePicReference(contact.email).getBytes(1024*1024).addOnSuccessListener {
            holder.profilePic.setImageBitmap(BitmapFactory.decodeByteArray(it,0,it.size))
        }
      //  holder.profilePic.setImageURI(contact.profilePhoto)
    }

    inner class ContactsViewHolder(view:View):RecyclerView.ViewHolder(view){
        var name=view.findViewById<TextView>(R.id.contacts_person_name)
        val profilePic=view.findViewById<ImageView>(R.id.contacts_person_profilePhoto)
        val email=view.findViewById<TextView>(R.id.contacts_email)
        var tickMark=view.findViewById<ImageView>(R.id.marked_imageView)
        var bg=view.findViewById<RelativeLayout>(R.id.item_background)
        var optionsButton=view.findViewById<ImageView>(R.id.options_ImageView)
        var context=view.context

        fun markItem(){
            tickMark.visibility=View.VISIBLE
            bg.setBackgroundResource(R.drawable.selected_item_background)
        }
        fun unMarkItem(){
            tickMark.visibility=View.INVISIBLE
            bg.setBackgroundResource(R.drawable.not_selected_item_background)
        }

    }

    fun updateContactList(list:MutableList<Contact>){
        contactList=list
        notifyDataSetChanged()
    }
    fun updateContactList(){
        notifyDataSetChanged()
    }
}