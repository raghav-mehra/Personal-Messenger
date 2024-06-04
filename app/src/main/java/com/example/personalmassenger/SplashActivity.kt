package com.example.personalmassenger

import Utils.Constants
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var imageGif:ImageView
    private lateinit var anim:Animation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        val mediaPlayer=MediaPlayer.create(this,R.raw.logo_sound)
        val activityIntent=Intent(this@SplashActivity,SignUpActivity::class.java)
      //  getToken()
        if(intent.extras!=null){
            val userEmail= intent.extras!!.getString(Constants.KEY_EMAIL).toString()
            Log.d("Push Notification",userEmail)
            FirebaseFirestore.getInstance().collection("Users").document(userEmail).get().addOnSuccessListener {
                val bundle=Bundle()
                bundle.putString(Constants.KEY_EMAIL,userEmail)
               bundle.putString(Constants.KEY_ID,it.getString(Constants.KEY_UID))
                bundle.putString(Constants.KEY_USERNAME,it.getString(Constants.KEY_USERNAME))
                activityIntent.putExtras(bundle)
            }
        }

        imageGif=findViewById(R.id.logo_imageview)
        anim=AnimationUtils.loadAnimation(this,R.anim.splash_1)
        imageGif.animation=anim
        Handler(Looper.getMainLooper()).postDelayed({
            mediaPlayer.start()
            startActivity(activityIntent)
            finish()
        },4000)
    }
    fun getToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d("Token",it.toString())
        }
    }

}
