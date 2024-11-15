package com.example.personalmassenger

import Utils.Constants
import Utils.FirebaseUtil
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.example.personalmassenger.adapters.ChatsRecyclerViewAdapter
import com.example.personalmassenger.adapters.ViewPagerAdapter
import com.example.personalmassenger.viewModel.ToolbarViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mikhaellopez.circularimageview.CircularImageView


class MainActivity : AppCompatActivity(), MainToolbar {
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var appPagerAdapter: ViewPagerAdapter
    private lateinit var drawer: DrawerLayout
    private lateinit var navHeaderEmail: TextView
    private lateinit var navHeaderName: TextView
    private lateinit var navProfilePic: CircularImageView
    private lateinit var headerView: View
    private lateinit var navigationView: NavigationView
    private var toolbarViewModel=ToolbarViewModel()
    private lateinit var chatsAdapterRef: ChatsRecyclerViewAdapter
    private var isSelected=false
    private lateinit var toggle:ActionBarDrawerToggle
    private val titles= arrayListOf("Chats","Highlights")
    private var queryRegistration: ListenerRegistration? = null
    private lateinit var mainToolbar: MainToolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setReferences()
        getFCMToken()
        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.sign_out_menu-> {
                    FirebaseUtil.firebaseAuth().signOut()
                    val intent= Intent(this,SignUpActivity::class.java)
                    startActivity(intent)
                    this.finish()
                }
                R.id.delete_menu->{
                    alertDeleteDialog()
                }
            }
            true
        }

//        toolbarViewModel.itemSelected.observe(this, Observer {
//            toolbarMode->
//            Log.d("ToolbarMode",toolbarMode.toString())
//                if(toolbarMode>=1){
//                    val deleteItem=toolbar.menu.findItem(R.id.delete_menu)
//                    deleteItem.isVisible=true
//                }
//            else{
//                    val deleteItem=toolbar.menu.findItem(R.id.delete_menu)
//                    deleteItem.isVisible=false
//                }
//        })
        appPagerAdapter= ViewPagerAdapter(this)
        viewPager.adapter=appPagerAdapter
        TabLayoutMediator(tabLayout,viewPager){
                tab,position->
            tab.text=titles[position]
        }.attach()
        navigationView.setCheckedItem(R.id.chat_nav)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_contacts -> {
                    val intent= Intent(this,ContactsActivity::class.java)
                    startActivity(intent)
                }
                R.id.profile_nav ->{
                    val intent= Intent(this,ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.new_group_nav->{
                    Toast.makeText(this,"This feature is yet to be included...",
                        Toast.LENGTH_SHORT).show()
                }
            }
            drawer.closeDrawer(GravityCompat.START)
            true
        }
//        queryRegistration=chatsDataReference.addSnapshotListener{ snapshot, error->
//            error?.let {
//                return@addSnapshotListener
//            }
//            snapshot?.let {
//
//            }
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(applicationContext)
                .withPermission(Manifest.permission.POST_NOTIFICATIONS)
                .withListener(object : PermissionListener{
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        p1: PermissionToken?
                    ) {

                    }

                }).check()
        }
        val policy=StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }
    private fun setReferences(){
        setContentView(R.layout.activity_main_container)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        toolbar=findViewById(R.id.main_toolbar)
        FirebaseApp.initializeApp(this)
        // toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
        tabLayout=findViewById(R.id.tab_layout)
        viewPager=findViewById(R.id.view_pager)
        drawer=findViewById(R.id.drawer_layout)
        navigationView=findViewById(R.id.navigation_view)
        toggle= ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
       // setSupportActionBar(toolbar)
        // toolbar.inflateMenu(R.menu.main_menu)
        headerView=navigationView.getHeaderView(0)
        navProfilePic=headerView.findViewById(R.id.imageView_header)
        navHeaderEmail=headerView.findViewById(R.id.nav_id)
        navHeaderName=headerView.findViewById(R.id.nav_username)
        navHeaderEmail.text=FirebaseUtil.currentUserEmail()
        FirebaseUtil.profilePicReference(FirebaseUtil.currentUserEmail()).getBytes(800 * 800)
            .addOnSuccessListener {
                navProfilePic.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
            }
        FirebaseUtil.currentUserDetails().get().addOnSuccessListener {
            navHeaderName.text=it.getString(Constants.KEY_USERNAME).toString()
//            navProfilePic.setImageURI(it.get(Constants.KEY_PROFILE_PHOTO) as Uri?)

        }
        //
    }
    override fun itemSelected() {
        val deleteItem=toolbar.menu.findItem(R.id.delete_menu)
        deleteItem.isVisible=true
    }

    override fun itemNotSelected() {
        val deleteItem=toolbar.menu.findItem(R.id.delete_menu)
        deleteItem.isVisible=false
    }

    override fun deleteSelectedItems(chatsAdapterRef: ChatsRecyclerViewAdapter) {
        this.chatsAdapterRef=chatsAdapterRef
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.sign_out_menu ->{
                FirebaseUtil.firebaseAuth().signOut()
                val intent= Intent(this,SignUpActivity::class.java)
                startActivity(intent)
                this.finish()
            }
            R.id.delete_menu->{
                alertDeleteDialog()

            }
        }
       // return true
        return super.onOptionsItemSelected(item)
    }
    private fun alertDeleteDialog(){
        val alertDialog= AlertDialog.Builder(this)
        alertDialog.setTitle("Confirm Deletion")
            .setMessage("All chat with selected users will be deleted. This action cannot be undone. Delete selected chats?")
            .setIcon(R.drawable.warning_svgrepo_com)
            .setPositiveButton("Yes"){dialog,_->
                chatsAdapterRef.deleteSelectedChats()
                itemNotSelected()
                dialog.dismiss()
            }
            .setNegativeButton("No"){dialog,_->
                dialog.dismiss()
            }.create().show()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (chatsAdapterRef.selectedItems != 0) {
            chatsAdapterRef.deselectAll()
        } else {
            super.onBackPressed()
        }
    }

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


//    override fun onBackPressed() {
//        val fragment =
//            this.supportFragmentManager.findFragmentById(R.id.main_fragment_container)
//        (fragment as? IOnBackPressed)?.onBackPressed()?.not()?.let {
//            super.onBackPressed()
//        }
//
//    }





