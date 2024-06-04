package com.example.personalmassenger.fragments

import Utils.FirebaseUtil
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalmassenger.Communicator
import com.example.personalmassenger.R
import com.example.personalmassenger.adapters.ChatsRecyclerViewAdapter
import com.example.personalmassenger.adapters.ContactsRecyclerViewAdapter
import com.example.personalmassenger.localDatabse.localDbHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import model.Contact


class Contacts : Fragment() {
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contactsAdapter = ContactsRecyclerViewAdapter()
        contactsAdapter.updateContactList(localDb.getAllContacts())
        contactsRecycler.adapter = contactsAdapter
        contactsRecycler.layoutManager = LinearLayoutManager(context)
        floatingAddButton.setOnClickListener {
            showNextAnimation()
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
                    Log.d("Edit","done")
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
                        viewFlipper.showPrevious()
                        contactsAdapter.updateContactList(localDb.getAllContacts())
                    }
                }
            }
        }
        contactsAdapter.editContact={
            tempContact=it
            showNextAnimation()
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
            communicator = activity as Communicator
            communicator.passData(
                contact.uid, contact.userName, contact.email
            )
        }
        contactsAdapter.onLongClickItem = {

        }
    }

    private fun editDialog() {
        val dialog=Dialog(activity?.applicationContext!!)
      //  dialog.setContentView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        localDb = localDbHandler(activity?.applicationContext!!)
        floatingAddButton = view.findViewById(R.id.contacts_add_floating_button)
        contactsRecycler = view.findViewById(R.id.contacts_recyclerview)
        viewFlipper = view.findViewById(R.id.contacts_flipper)
        addButton = view.findViewById(R.id.add_contact_button)
        inputName = view.findViewById(R.id.add_contact_input_name)
        inputEmail = view.findViewById(R.id.add_contact_input_email)
        toolbar = view.findViewById(R.id.contacts_toolbar)
        return view
    }

    private fun showNextAnimation() {
        viewFlipper.setOutAnimation(context, android.R.anim.slide_in_left)
        viewFlipper.setOutAnimation(context, android.R.anim.slide_out_right)
    }

    private fun showPreviousAnimation() {
        viewFlipper.setInAnimation(context, R.anim.slide_in_right)
        viewFlipper.setOutAnimation(context, R.anim.slide_out_left)
    }

//    override fun onBackPressed(): Boolean {
//        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_fragment_container,MainFragment())?.commit()
//        return false
//    }


}