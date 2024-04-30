package com.example.personalmassenger.ui.theme

import Utils.Constants
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.example.personalmassenger.R
import com.example.personalmassenger.fragments.Contacts

class MenuActivity : AppCompatActivity() {
    private lateinit var toolbar:Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        toolbar=findViewById(R.id.menu_toolbar)
        setSupportActionBar(toolbar)
        when(intent.getStringExtra(Constants.KEY_OPTION)){
            Constants.KEY_CONTACTS->{
                toolbar.title="Contacts"
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, Contacts()).commit()
            }
        }


    }
}