package com.example.personalmassenger.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import model.ChatInfo

class NewViewModel:ViewModel() {
    var initial:Int=0
    val chatList:MutableLiveData<MutableList<ChatInfo>> by lazy {
        MutableLiveData<MutableList<ChatInfo>>()
    }
    val chatChanges:MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

}