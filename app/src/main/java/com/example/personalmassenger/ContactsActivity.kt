package com.example.personalmassenger

import Utils.Constants
import Utils.FirebaseUtil
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalmassenger.adapters.ContactsRecyclerViewAdapter
import com.example.personalmassenger.localDatabse.localDbHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import model.Contact

class ContactsActivity : AppCompatActivity() {
    private lateinit var floatingAddButton: FloatingActionButton
    private lateinit var addButton: Button
    private lateinit var inputName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var toolbar: Toolbar
    private lateinit var contactsAdapter: ContactsRecyclerViewAdapter
    private lateinit var contactsRecycler: RecyclerView
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var localDb: localDbHandler
    private lateinit var communicator: Communicator
    private lateinit var tempContact:Contact
    private var edit=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        localDb = localDbHandler(this)
        floatingAddButton = findViewById(R.id.contacts_add_floating_button)
        contactsRecycler = findViewById(R.id.contacts_recyclerview)
        viewFlipper = findViewById(R.id.contacts_flipper)
        addButton = findViewById(R.id.add_contact_button)
        inputName = findViewById(R.id.add_contact_input_name)
        inputEmail = findViewById(R.id.add_contact_input_email)
        toolbar = findViewById(R.id.contacts_toolbar)
        contactsAdapter = ContactsRecyclerViewAdapter()
        contactsAdapter.updateContactList(localDb.getAllContacts())
        contactsRecycler.adapter = contactsAdapter
        contactsRecycler.layoutManager = LinearLayoutManager(this)
        floatingAddButton.setOnClickListener {
            showNextAnimation()
            toolbar.setTitle(R.string.add_contact)
            viewFlipper.displayedChild = 1
        }
        addButton.setOnClickListener {
            val name = inputName.text.toString()
            val email = inputEmail.text.toString()
            if (name.isEmpty()) {
                inputName.error = "You should provide some name"
            }
            if (email.isEmpty()) {
                inputEmail.error = "Please enter email"
            }
            if (!email.isEmpty() && !name.isEmpty()) {
                if(edit){
                    tempContact.email=email
                    tempContact.userName=name
                    showPreviousAnimation()
                    viewFlipper.showPrevious()
                    localDb.updateContact(tempContact)
                    contactsAdapter.updateContactList(localDb.getAllContacts())
                    addButton.setText(R.string.add_contact)
                    inputEmail.setText("")
                    inputName.setText("")
                    edit=false
                }
                else {
                    FirebaseUtil.userDetails(email).get().addOnSuccessListener { doc ->
                        localDb.addContact(
                            Contact(
                                name,
                                "",
                                doc.getString("uid").toString(),
                                email
                            )
                        )
                        showPreviousAnimation()
                        toolbar.setTitle(R.string.contacts)
                        viewFlipper.showPrevious()
                        contactsAdapter.updateContactList(localDb.getAllContacts())
                    }
                }
            }
        }
        contactsAdapter.editContact={
            tempContact=it
            showNextAnimation()
            toolbar.setTitle(R.string.edit_contact)
            viewFlipper.displayedChild=1
            inputName.setText(it.userName)
            inputEmail.setText(it.email)
            addButton.setText(R.string.save)
            edit=true
        }
        contactsAdapter.deleteContact={
            localDb.deleteContact(it)
        }
        contactsAdapter.onItemClick = { contact ->
            val bundle = Bundle()
            bundle.putString(Constants.KEY_ID, contact.uid)
            bundle.putString(Constants.KEY_USERNAME, contact.userName)
            bundle.putString(Constants.KEY_EMAIL, contact.email)
            val intent= Intent(this,MessagingActivity::class.java)
            intent.putExtra(Constants.KEY_REFERENCE,bundle)
            startActivity(intent)
        }
        contactsAdapter.onLongClickItem = {

        }
    }
    private fun showNextAnimation() {
        viewFlipper.setOutAnimation(this, android.R.anim.slide_in_left)
        viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right)
    }

    private fun showPreviousAnimation() {
        viewFlipper.setInAnimation(this, R.anim.slide_in_right)
        viewFlipper.setOutAnimation(this, R.anim.slide_out_left)
    }
}