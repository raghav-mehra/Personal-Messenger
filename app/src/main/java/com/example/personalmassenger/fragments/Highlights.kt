package com.example.personalmassenger.fragments

import Utils.Constants
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.compose.ui.unit.Constraints
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.circularstatusview.CircularStatusView
import com.example.personalmassenger.Communicator
import com.example.personalmassenger.R
import com.example.personalmassenger.adapters.HighlightsAdapter
import com.example.personalmassenger.localDatabse.localDbHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import model.Highlight
import xute.storyview.StoryModel
import java.sql.Timestamp
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class Highlights : Fragment() {
    private lateinit var myStatusDp:ImageView
    private lateinit var recyclerView:RecyclerView
    private lateinit var myCircularStatusView:CircularStatusView
    private lateinit var localDb:localDbHandler
    private lateinit var storyProgress:CircularProgressBar
    private var auth=FirebaseAuth.getInstance().currentUser
    private var adapter=HighlightsAdapter()
    private var portionsCount=0
    private lateinit var addStoryButton: ImageView
    private  var storyStorage= FirebaseStorage.getInstance().reference.child("stories")
    private lateinit var communicator: Communicator
    private var userReference=FirebaseFirestore.getInstance().collection("Users")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_highlights, container, false)
        localDb=localDbHandler(activity?.applicationContext!!)
        myStatusDp=view.findViewById(R.id.highlights_my_photo)
        storyProgress=view.findViewById(R.id.story_progress_bar)
        recyclerView=view.findViewById(R.id.highlights_recycler)
        addStoryButton=view.findViewById(R.id.add_story_button)
        myCircularStatusView=view.findViewById(R.id.my_circular_status_view)
        recyclerView.adapter=adapter
        recyclerView.layoutManager=LinearLayoutManager(activity)
        checkForHighlights()
        addStoryButton.setOnClickListener {
            uploadStory()
        }
        myStatusDp.setOnClickListener {
            if(portionsCount>0) {
                communicator = activity as Communicator
                communicator.passStoryData(auth?.displayName.toString(), auth?.email.toString())
            }
        }
        adapter.onItemClick={

            communicator=activity as Communicator
            communicator.passStoryData(it.name,it.email)

        }
        userReference.document(auth?.email!!).collection("Story").get().addOnSuccessListener   { myStories->
            var cnt=0
            for(story in myStories!!){
                val timeStamp=story.getString(Constants.KEY_TIME_STAMP)?.toLong()!!
                val duration= TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - timeStamp)
                Log.d("hours",duration.toString())
                if(duration<24){
                    cnt+=1
                }
                else{
                    userReference.document(auth?.email!!).collection("Story").document(story.id).delete()

                }
            }
            portionsCount=cnt
            myCircularStatusView.setPortionsCount(portionsCount)
            myCircularStatusView.invalidate()
        }

        return view
    }

    private fun uploadStory(){
        myCircularStatusView.visibility=View.INVISIBLE
        val intent=Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/* video/*")
        startActivityForResult(intent,2)
    }
    private fun showStoryLoading(){
        myCircularStatusView.visibility=View.INVISIBLE
        myCircularStatusView.invalidate()
        storyProgress.visibility=View.VISIBLE

    }
    private fun hideStoryLoading(){
        storyProgress.visibility=View.INVISIBLE
        myCircularStatusView.visibility=View.VISIBLE
        myCircularStatusView.invalidate()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode==RESULT_OK){
            val imageUri=data?.data

            if(imageUri!=null){
                showStoryLoading()
                storyStorage.child("${auth?.uid!!}/${System.currentTimeMillis()}").putFile(imageUri).addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener {
                        hideStoryLoading()
                        val map= mutableMapOf<String,String>()
                        map[Constants.KEY_IMAGE_URI]=it.toString()
                        map[Constants.KEY_TIME_STAMP]=System.currentTimeMillis().toString()
                        userReference.document(auth?.email!!).collection("Story").add(map).addOnSuccessListener {
                            myCircularStatusView.setPortionsCount(++portionsCount)
                            myCircularStatusView.invalidate()
                        }
                    }.addOnFailureListener {
                        hideStoryLoading()
                    }
                }.addOnFailureListener {
                    hideStoryLoading()
                }
            }
            else{
                hideStoryLoading()
            }
        }
    }
    private fun checkForHighlights(){
        val contacts = localDb.getAllContacts()
        val highlights= mutableListOf<Highlight>()
        for (contact in contacts) {
            val email = contact.email
            userReference.document(email).collection("Story").get()
                .addOnSuccessListener { userStories ->
                    var cnt = 0
                    //val time=TimeUnit(userStories.last().getString(Constants.KEY_TIME_STAMP))
                    for (story in userStories!!) {
                        val timeStamp = story.getString(Constants.KEY_TIME_STAMP)?.toLong()!!
                        val duration =
                            TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - timeStamp)
                        if (duration < 24) {
                            cnt += 1
                        } else {
                            userReference.document(email).collection("Story").document(story.id)
                                .delete()
                        }
                    }
                    if (cnt>0) {
                        adapter.update(Highlight(contact.userName,email, "", "", cnt))
                    }

                }
        }

    }

    override fun onResume() {
        super.onResume()

    }



}