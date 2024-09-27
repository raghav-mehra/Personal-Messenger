package com.example.personalmassenger.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.personalmassenger.MainToolbar

class ToolbarViewModel:ViewModel() {
    private var _itemSelected = MutableLiveData<Int>()
    var itemSelected : LiveData<Int> = _itemSelected
    var num=0
    init {
        _itemSelected.postValue(num)
    }
    fun showOptionsForSelectedItems(){
        Log.d("tool",itemSelected.value.toString())
        _itemSelected.postValue(++num)
    }
    fun hideOptionsForSelectedItems(){
        _itemSelected.postValue(0)
    }
}