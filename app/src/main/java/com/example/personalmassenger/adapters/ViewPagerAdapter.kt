package com.example.personalmassenger.adapters
import com.example.personalmassenger.fragments.Chats
import com.example.personalmassenger.fragments.Highlights
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity):FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> {Chats()}
            1->Highlights()
            else -> Chats()
        }
    }

}
