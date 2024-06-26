package com.example.personalmassenger

import com.example.personalmassenger.adapters.ChatsRecyclerViewAdapter

interface Communicator {
    fun passData(userId:String,userName:String, email: String)
    fun passStoryData(name:String,reference:String)
    fun refreshChats()
    fun passChatsAdapterReference(adapter: ChatsRecyclerViewAdapter)
}