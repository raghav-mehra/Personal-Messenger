package com.example.personalmassenger

import Utils.Constants
import Utils.FirebaseUtil
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.util.Log
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.personalmassenger.adapters.ChatsRecyclerViewAdapter
import com.example.personalmassenger.fragments.Chats
import com.example.personalmassenger.fragments.MainFragment
import com.example.personalmassenger.fragments.Messaging
import com.example.personalmassenger.fragments.StatusViewer
import com.example.personalmassenger.viewModel.NewViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity(), Communicator,MainToolbar {
    lateinit var chatadapter:ChatsRecyclerViewAdapter
    lateinit var homeFragment:MainFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)
        val transaction = this.supportFragmentManager.beginTransaction()
        homeFragment = MainFragment()
//        viewModel= ViewModelProvider(this).get(NewViewModel::class.java)
//        viewModel.chatChanges.observe(this, Observer {
//            Chats().onResume()
//        })
//        if (intent.extras != null) {
//            val bundle=intent.extras
//            passData(
//                bundle?.getString(Constants.KEY_UID).toString(),
//                bundle?.getString(Constants.KEY_USERNAME).toString(),
//                bundle?.getString(Constants.KEY_EMAIL).toString()
//            )
//        }
        getFCMToken()
//        else {
            transaction.replace(R.id.main_fragment_container, homeFragment)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction.commit()
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy()).detectLeakedClosableObjects()
                    .build()
            )
        }
    //}

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseUtil.currentUserDetails().update(Constants.KEY_TOKEN,task.result.toString())
                Log.i("Token", task.result)
            }
        }
    }

    override fun passData(userId:String,userName:String, email: String) {
        val bundle = Bundle()
        bundle.putString(Constants.KEY_ID, userId)
        bundle.putString(Constants.KEY_USERNAME, userName)
        bundle.putString(Constants.KEY_EMAIL, email)
        val transaction = this.supportFragmentManager.beginTransaction()
        val messagingFragment = Messaging()
        messagingFragment.arguments = bundle
        transaction.replace(R.id.main_fragment_container, messagingFragment)
        transaction.addToBackStack(null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    override fun passStoryData(name: String, reference: String) {
        val transaction = this.supportFragmentManager.beginTransaction()
        val statusViewerFragment = StatusViewer()
        val bundle = Bundle()
        bundle.putString(Constants.KEY_REFERENCE, reference)
        bundle.putString(Constants.KEY_NAME, name)
        statusViewerFragment.arguments = bundle
        transaction.replace(R.id.main_fragment_container, statusViewerFragment)
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
        transaction.addToBackStack(null).commit()
    }

    override fun refreshChats() {
       chatadapter.refresh()
    }

    override fun passChatsAdapterReference(adapter: ChatsRecyclerViewAdapter) {
        chatadapter=adapter
    }

    override fun itemSelected() {
        homeFragment.updateToolbar()
    }

    override fun deleteSelectedItems() {
        chatadapter.deleteSelectedChats()
    }
//    override fun onBackPressed() {
//        val fragment =
//            this.supportFragmentManager.findFragmentById(R.id.main_fragment_container)
//        (fragment as? IOnBackPressed)?.onBackPressed()?.not()?.let {
//            super.onBackPressed()
//        }
//
//    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main_menu,menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.sign_out_menu ->{
//                auth.signOut()
//                val intent=Intent(this@MainActivity,SignUpActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
    override fun onBackPressed() {
        super.onBackPressed()
        homeFragment.resetToolbar()

    }

}