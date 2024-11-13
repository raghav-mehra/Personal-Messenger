package com.example.personalmassenger.viewModel

import LLD.MessagingManager.MessageObjectAdapter.MessageObjectAdapter
import Utils.Constants
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.personalmassenger.adapters.MessageAdapter
import com.example.personalmassenger.localDatabse.localDbHandler
import model.ChatInfo
import model.Message

class MessagingViewModel(context:Context,tableName:String):ViewModel() {

    private val localDb=localDbHandler.getInstance(context)
    private var _messagesList = MutableLiveData<MutableList<Message>> ()
     var messagesList = localDb.getFullChat(tableName)
    var messageList: LiveData<MutableList<Message>> = _messagesList
    private var messageAdapter = MessageObjectAdapter()

    init {
        _messagesList.postValue(messagesList)
    }

    fun addMessage(message: Message){
         messagesList.add(message)
        _messagesList.postValue(messagesList)
        localDb.addMessage(Constants.getTableName(message.senderEmail),message)
    }

    fun addMessage(message: Map<String,String>){
        val messageModel=messageAdapter.getMessageModel(message)
        messagesList.add(messageModel)
        _messagesList.postValue(messagesList)
        localDb.addMessage(Constants.getTableName(messageModel.receiverEmail),messageModel)
    }

    fun observeMessagingLiveData(): MutableLiveData<MutableList<Message>> {
        return _messagesList
    }

}