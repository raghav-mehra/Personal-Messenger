package com.example.personalmassenger.localDatabse

import Utils.Constants
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import model.ChatInfo
import model.Contact
import model.Message

class localDbHandler(context: Context) :
    SQLiteOpenHelper(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION) {
        private var contactName= mutableMapOf <String,String>()

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("databaseName", Constants.DATABASE_NAME)
        val CREATE_TABLE_CONTACTS = "CREATE TABLE ${Constants.TABLE_NAME_CONTACTS} (" +
                "${Constants.KEY_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${Constants.KEY_NAME} TEXT," +
                "${Constants.KEY_TABLE_NAME} TEXT," +
                "${Constants.KEY_EMAIL} TEXT)"

        db?.execSQL(CREATE_TABLE_CONTACTS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS ${Constants.TABLE_NAME_CONTACTS}")
        onCreate(db)
    }
    fun generateMap(){
        val db = this.writableDatabase
        val selectAll = "SELECT * FROM ${Constants.TABLE_NAME_CONTACTS}"
        val cursor = db.rawQuery(selectAll, null)
        if (cursor.moveToFirst()){
            do{
                contactName[cursor.getString(2)]=cursor.getString(1)
            }while (cursor.moveToNext())

        }
    }

    fun createTable(tableName: String) {
        val db = this.writableDatabase
        val CREATE_TABLE_CHAT = "CREATE TABLE IF NOT EXISTS ${tableName} (" +
                "${Constants.KEY_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${Constants.KEY_SENDER_ID} TEXT," +
                "${Constants.KEY_MESSAGE} TEXT," +
                "${Constants.KEY_TIME_STAMP} TEXT," +
                "${Constants.KEY_EMAIL} TEXT,"+
                "${Constants.KEY_IMAGEPATH} TEXT)"
        db.execSQL(CREATE_TABLE_CHAT)
    }

    fun addMessage(tableName: String, message: Message) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(Constants.KEY_SENDER_ID, message.senderId)
        values.put(Constants.KEY_MESSAGE, message.message)
        values.put(Constants.KEY_TIME_STAMP, message.timeStamp)
        values.put(Constants.KEY_EMAIL, message.email)
        values.put(Constants.KEY_IMAGEPATH,message.imagePath)
        db.insert(tableName, null, values)
        db.close()
    }

    fun addContact(contact: Contact) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(Constants.KEY_NAME, contact.userName)
        values.put(Constants.KEY_TABLE_NAME,Constants.getTableName(contact.email))
        values.put(Constants.KEY_EMAIL, contact.email)
        db.insert(Constants.TABLE_NAME_CONTACTS, null, values)
        contactName[contact.uid]=contact.userName
        db.close()
    }

    fun getAllContacts(): MutableList<Contact> {
        val db = this.writableDatabase
        val contacts = mutableListOf<Contact>()
        val selectAll = "SELECT * FROM ${Constants.TABLE_NAME_CONTACTS}"
        val count="SELECT count(*) FROM ${Constants.TABLE_NAME_CONTACTS}"
        val cursor = db.rawQuery(selectAll, null)
        val cursor2=db.rawQuery(count,null)
        cursor2.moveToFirst()
        if (cursor2.getInt(0)>0 && cursor.moveToFirst()) {
            do {
                contacts.add(
                    Contact(
                        cursor.getString(1), "", cursor.getString(2),cursor.getString(3)
                    )
                )
            } while (cursor.moveToNext())
        }
        db.close()
        return contacts
    }

    fun getFullChat(tableName: String): MutableList<Message> {
        val db = this.writableDatabase
        createTable(tableName)
        val messages = mutableListOf<Message>()
        val selectAll = "SELECT * FROM ${tableName}"
        val cursor = db.rawQuery(selectAll, null)
        if (cursor.moveToFirst()) {
            do {
                messages.add(
                    Message(
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5)
                    )
                )
            } while (cursor.moveToNext())
        }
        db.close()
        return messages
    }

    @SuppressLint("SuspiciousIndentation")

    fun loadChatInfo(): MutableList<ChatInfo> {
        val db = this.writableDatabase
        generateMap()
        val chats = mutableListOf<ChatInfo>()
        val tableExceptions= listOf<String>(Constants.TABLE_NAME_CHATS,Constants.TABLE_NAME_CONTACTS,"android_metadata")
        val selectAllTables = "SELECT name FROM sqlite_master WHERE type='table' AND NAME NOT LIKE 'sqlite_%'"
        val cursor = db.rawQuery(selectAllTables, null)
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val tableName = cursor.getString(0)
                Log.d("tableName",tableName)
                if (tableName in tableExceptions) {
                    cursor.moveToNext()
                    continue
                }
                val count = "SELECT count(*) FROM ${tableName}"
                val cursor2 = db.rawQuery(count, null)
                cursor2.moveToFirst()
                if(cursor2.getInt(0)>0)
                chats.add(fetchLastRow(tableName))
                cursor.moveToNext()
            }
        }
         return chats
    }

    private fun fetchLastRow(tableName: String): ChatInfo {
        val db = this.writableDatabase
        val selectAll = "SELECT * FROM ${tableName}"
        val cursor = db.rawQuery(selectAll, null)
        cursor.moveToLast()
        val name=if(contactName[tableName]!=null) contactName[tableName].toString() else getEmail(tableName)

        val info=ChatInfo(
            name,
            "",
            Pair(cursor.getString(2), cursor.getString(3)),
            tableName+"@gmail.com",cursor.getString(4)
        )

        return info
    }
    fun deleteTableRecord(tableName: String){
        val db=this.writableDatabase
        db.delete(tableName,null,null)
    }

    fun getContact(uid: String): Contact {
        val db = this.writableDatabase
        val cursor = db.query(
            Constants.TABLE_NAME_CONTACTS,
            arrayOf(Constants.KEY_ID, Constants.KEY_NAME, Constants.KEY_TABLE_NAME,Constants.KEY_EMAIL),
            Constants.KEY_TABLE_NAME + "=?",
            arrayOf(uid),
            null,
            null,
            null,
            null
        )
        cursor?.moveToFirst()
        return Contact(cursor.getString(1), "", cursor.getString(2),cursor.getString(3))
    }
    fun getEmail(uid:String): String{
        val db = this.writableDatabase
        val cursor = db.query(
            uid,
            arrayOf(Constants.KEY_ID, Constants.KEY_SENDER_ID, Constants.KEY_MESSAGE,Constants.KEY_TIME_STAMP,Constants.KEY_EMAIL,Constants.KEY_IMAGEPATH),
            Constants.KEY_SENDER_ID + "=?",
            arrayOf(uid),
            null,
            null,
            null,
            null
        )
        cursor?.moveToFirst()
        return try {
            cursor.getString(4)
        } catch (e:Exception){
            "Unknown"
        }

    }

    fun getContactsCount(): Int {
        val db = this.writableDatabase
        val selectAll = "SELECT * FROM ${Constants.TABLE_NAME_CONTACTS}"
        val cursor = db.rawQuery(selectAll, null)

        return cursor.count
    }

    fun updateContact(contact: Contact): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(Constants.KEY_NAME, contact.userName)
        values.put(Constants.KEY_EMAIL, contact.email)
        return db.update(
            Constants.TABLE_NAME_CONTACTS, values, Constants.KEY_TABLE_NAME + "=?",
            arrayOf(contact.uid)
        )
    }
    fun deleteContact(contact: Contact){
        val db=this.writableDatabase
        db.execSQL("delete from "+"${Constants.TABLE_NAME_CONTACTS}"+" where ${Constants.KEY_EMAIL}= '${contact.email}'")
    }

}