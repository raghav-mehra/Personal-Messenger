package com.example.personalmassenger.ui.theme

import model.ChatInfo

class GetChats {
    fun generateChats():MutableList<ChatInfo>{
        val list= mutableListOf<ChatInfo>()
//        for (i in 0 until 10){
//            val pair:Pair<String,String> =Pair("Hello Bro! $i","$i : 15")
//            list.add(ChatInfo("Chat $i","",pair,null))
//        }
        return list
    }
}