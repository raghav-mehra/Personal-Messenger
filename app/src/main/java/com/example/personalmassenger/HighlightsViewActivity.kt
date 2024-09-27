package com.example.personalmassenger

import Utils.Constants
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import gr.makris.androidstories.Stories
import gr.makris.androidstories.data.StoryItem

class HighlightsViewActivity : AppCompatActivity() {
    private lateinit var storyView: Stories
    private lateinit var storyReference: CollectionReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_highlights_view)
        val reference=intent.getBundleExtra(Constants.KEY_REFERENCE)
        val email=reference?.getString(Constants.KEY_EMAIL).toString()
        val name=reference?.getString(Constants.KEY_NAME).toString()
        storyReference = FirebaseFirestore.getInstance().collection("Users").document(email).collection("Story")
        storyView = findViewById(R.id.stories)
        setStories()
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