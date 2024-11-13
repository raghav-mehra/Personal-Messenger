package model

data class NotificationData(
    val token:String?,
    val data:HashMap<String,String>? = null
)
