package com.example.personalmassenger

import com.example.personalmassenger.adapters.ChatsRecyclerViewAdapter

interface MainToolbar {
    fun itemSelected()
    fun itemNotSelected()
    fun deleteSelectedItems(chatsAdapterRef:ChatsRecyclerViewAdapter)
}