package com.example.personalmassenger.fragments

import Utils.Constants
import Utils.FirebaseUtil
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.personalmassenger.MainToolbar
import com.example.personalmassenger.R
import com.example.personalmassenger.SignUpActivity
import com.example.personalmassenger.adapters.ViewPagerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mikhaellopez.circularimageview.CircularImageView

class MainFragment : Fragment(){
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var appPagerAdapter: ViewPagerAdapter
    private lateinit var drawer: DrawerLayout
    private lateinit var navHeaderEmail:TextView
    private var isSelected=false
    private lateinit var navHeaderName:TextView
    private lateinit var navProfilePic:CircularImageView
    private lateinit var headerView: View
    private lateinit var navigationView:NavigationView
    private lateinit var toggle:ActionBarDrawerToggle
    private val titles= arrayListOf("Chats","Highlights")
    private var queryRegistration: ListenerRegistration? = null
    private lateinit var mainToolbar: MainToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_main, container, false)

        toolbar=view.findViewById(R.id.main_toolbar)
        toolbar.navigationIcon?.setColorFilter(Color.WHITE,PorterDuff.Mode.MULTIPLY)
        tabLayout=view.findViewById(R.id.tab_layout)
        viewPager=view.findViewById(R.id.view_pager)
        drawer=view.findViewById(R.id.drawer_layout)
        navigationView=view.findViewById(R.id.navigation_view)
        toggle= ActionBarDrawerToggle(context as FragmentActivity,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        mainToolbar=activity as MainToolbar
        drawer.addDrawerListener(toggle)
        toggle.syncState()
       // toolbar.inflateMenu(R.menu.main_menu)
        headerView=navigationView.getHeaderView(0)
        navProfilePic=headerView.findViewById(R.id.imageView)
        navHeaderEmail=headerView.findViewById(R.id.nav_id)
        navHeaderName=headerView.findViewById(R.id.nav_username)
        navHeaderEmail.text=FirebaseUtil.currentUserEmail()
        FirebaseUtil.currentUserDetails().get().addOnSuccessListener {
            navHeaderName.text=it.getString(Constants.KEY_USERNAME).toString()

//            navProfilePic.setImageURI(it.get(Constants.KEY_PROFILE_PHOTO) as Uri?)
        }

        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.sign_out_menu-> {
                    FirebaseUtil.firebaseAuth().signOut()
                    val intent=Intent(activity?.applicationContext,SignUpActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
                R.id.delete_menu->{
                    mainToolbar.deleteSelectedItems()
                    resetToolbar()
                }
            }
            true
        }

        appPagerAdapter= ViewPagerAdapter(context as FragmentActivity)
        viewPager.adapter=appPagerAdapter
        TabLayoutMediator(tabLayout,viewPager){
                tab,position->
            tab.text=titles[position]
        }.attach()
        navigationView.setCheckedItem(R.id.chat_nav)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_contacts -> {
                    val transaction=activity?.supportFragmentManager?.beginTransaction()
                    val contactsFragment=Contacts()
                    transaction?.replace(R.id.main_fragment_container,contactsFragment)
                    transaction?.addToBackStack(null)?.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
                    transaction?.commit()
                }
                R.id.profile_nav ->{
                    val transaction=activity?.supportFragmentManager?.beginTransaction()
                    val profileFragment=Profile()
                    transaction?.replace(R.id.main_fragment_container,profileFragment)
                    transaction?.addToBackStack(null)?.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
                    transaction?.commit()
                }
                R.id.new_group_nav->{
                    Toast.makeText(activity?.applicationContext,"This feature is yet to be included...",Toast.LENGTH_SHORT).show()
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
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        queryRegistration?.also {
            it.remove()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.main_menu,menu)
//    }

     fun updateToolbar(){
        isSelected=!isSelected
        val deleteItem=toolbar.menu.findItem(R.id.delete_menu)
        deleteItem.isVisible=isSelected
    }
    fun resetToolbar(){
        val deleteItem=toolbar.menu.findItem(R.id.delete_menu)
        deleteItem.isVisible=false
    }


}