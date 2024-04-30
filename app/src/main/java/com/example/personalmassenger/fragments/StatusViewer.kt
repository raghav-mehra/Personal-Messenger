package com.example.personalmassenger.fragments

import Utils.Constants
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.personalmassenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import gr.makris.androidstories.Stories
import gr.makris.androidstories.data.StoryItem
import xute.storyview.StoryModel
import xute.storyview.StoryView


class StatusViewer : Fragment() {
    private lateinit var storyView: Stories
    private lateinit var storyReference: CollectionReference




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_status_viewer, container, false)
        val reference=arguments?.getString(Constants.KEY_REFERENCE)
        storyReference = FirebaseFirestore.getInstance().collection("Users").document(reference.toString()).collection("Story")
        storyView = view.findViewById(R.id.stories)
        setStories()
        //storyView.setStoriesList(arrayListOf(StoryItem("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4")))
        return view
    }

    fun setStories() {
        val uriArray=ArrayList<StoryItem>()
        storyReference.orderBy(Constants.KEY_TIME_STAMP).get().addOnSuccessListener { value ->
            value.let { docs ->
                for (doc in docs!!) {
                    uriArray.add(StoryItem(doc.getString(Constants.KEY_IMAGE_URI).toString()))
                }

                storyView.setStoriesList(uriArray)
            }

        }
    }


}