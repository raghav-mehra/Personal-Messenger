package LLD.ChatsManager

import model.ChatInfo

interface ChatsUiInterface {
    fun selectItem(chat:ChatInfo)
    fun deselectItem(chat:ChatInfo)
    fun deselectAll()
    fun selectAll()
}