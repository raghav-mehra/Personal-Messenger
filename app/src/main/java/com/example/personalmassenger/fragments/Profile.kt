package com.example.personalmassenger.fragments

import Utils.Constants
import Utils.FirebaseUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.example.personalmassenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mikhaellopez.circularimageview.CircularImageView
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


class Profile : Fragment() {
    private lateinit var progressBar:CircularProgressBar
    private lateinit var saveButton: Button
    private lateinit var profilePhoto:CircularImageView
    private lateinit var nameEditText: EditText
    private lateinit var toolbar: Toolbar
    private val register=registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
        if (it!=null){
            uploadImage(it)
        }
    }
    private fun uploadImage(uri:Uri){
        val userId=FirebaseUtil.currentUserEmail()
        FirebaseUtil.profilePicReference(userId).putFile(uri).addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener {
                FirebaseUtil.currentUserDetails().update(Constants.KEY_PROFILE_PHOTO,it.toString())
                profilePhoto.setImageURI(uri)
            }
        }
    }
    private fun uploadImage(bitmap:Bitmap?){
        val baos= ByteArrayOutputStream()
        val userId=FirebaseUtil.currentUserEmail()
        bitmap?.compress(Bitmap.CompressFormat.JPEG,50,baos)
        val path = MediaStore.Images.Media.insertImage(activity?.contentResolver, bitmap, "Title", null)
        //    val image=baos.toByteArray()
        val imageUri= Uri.parse(path)
        FirebaseUtil.profilePicReference(userId).putFile(Uri.parse(path)).addOnSuccessListener {
            profilePhoto.setImageBitmap(bitmap)
//            profilePicReference.downloadUrl.addOnSuccessListener {
//                userReference.update("profilePhoto",it.toString())
//
//            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_profile, container, false)
        saveButton=view.findViewById(R.id.profile_save_button)
        progressBar=view.findViewById(R.id.profile_circularProgressBar)
        profilePhoto=view.findViewById(R.id.profile_profile_pic)
        nameEditText=view.findViewById(R.id.profile_user_name)
        toolbar=view.findViewById(R.id.profile_toolbar)
        FirebaseUtil.profilePicReference(FirebaseUtil.currentUserEmail()).getBytes(1024*1024).addOnSuccessListener {
            profilePhoto.setImageBitmap(BitmapFactory.decodeByteArray(it,0,it.size))
        }
        profilePhoto.setOnClickListener {
            register.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        FirebaseUtil.currentUserDetails().get().addOnSuccessListener {
           nameEditText.setText(it.getString(Constants.KEY_USERNAME))
        }
        saveButton.setOnClickListener {
            if (nameEditText.text.trim().isNotEmpty()){
                FirebaseUtil.currentUserDetails().update(Constants.KEY_USERNAME,nameEditText.text.toString()).addOnSuccessListener {
                    Toast.makeText(activity,"Successfully updated",Toast.LENGTH_SHORT).show()
                }
            }
        }
        return view
    }

}