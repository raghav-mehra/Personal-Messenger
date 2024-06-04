package com.example.personalmassenger


import Utils.FirebaseUtil
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.personalmassenger.databinding.SignupActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage.getInstance
import com.google.firebase.storage.StorageReference
import model.User
import java.io.ByteArrayOutputStream


class SignUpActivity : AppCompatActivity() {
    private lateinit var mBinding: SignupActivityBinding
    private lateinit var email:String
    private val register=registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
        uploadImage(it)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  val animation=AnimationUtils.loadAnimation(this,R.anim.slide)
        mBinding = DataBindingUtil.setContentView(this, R.layout.signup_activity)
        mBinding.textViewRegister.setOnClickListener {
            showNextAnimation()
            mBinding.flipper.showNext()
        }
        mBinding.textViewSignIn.setOnClickListener {
            showPreviousAnimation()
            mBinding.flipper.showPrevious()
        }
        mBinding.signInButton.setOnClickListener { SignIn() }
        mBinding.signUpButton.setOnClickListener { SignUp() }
        mBinding.signupProfilePhoto.setOnClickListener{
            capturePhoto()
        }
        mBinding.continueButton.setOnClickListener{launchMainActivity()}
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseUtil.firebaseAuth().currentUser!=null){
            launchMainActivity()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    private fun capturePhoto(){
        register.launch(null)
    }

    private fun uploadImage(bitmap:Bitmap?){
        val baos= ByteArrayOutputStream()
        val userEmail=FirebaseUtil.currentUserEmail()
        bitmap?.compress(Bitmap.CompressFormat.JPEG,50,baos)
        val path = MediaStore.Images.Media.insertImage(this.contentResolver, bitmap, "Title", null)
    //    val image=baos.toByteArray()
        val imageUri=Uri.parse(path)

        FirebaseUtil.profilePicReference(userEmail).putFile(Uri.parse(path)).addOnSuccessListener {
            Toast.makeText(this@SignUpActivity,"Successfully uploaded",Toast.LENGTH_SHORT).show()
            it.storage.downloadUrl.addOnSuccessListener {
                FirebaseUtil.currentUserDetails().update("profilePhoto",it.toString())
                mBinding.signupProfilePhoto.setImageBitmap(bitmap)
            }
        }.addOnFailureListener{
            Toast.makeText(this@SignUpActivity,"Unable to upload profile photo",Toast.LENGTH_SHORT).show()
        }
    }

    private fun SignIn() {
         email = mBinding.singInInputEmail.editText?.text.toString().trim()
        val password = mBinding.singInInputPassword.editText?.text.toString().trim()
        mBinding.circularProgressBar.visibility = View.VISIBLE

        FirebaseUtil.firebaseAuth().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@SignUpActivity, "Signed in", Toast.LENGTH_SHORT).show()
                    launchMainActivity()
                } else {
                    mBinding.circularProgressBar.visibility = View.INVISIBLE
//                    Toast.makeText(
//                        this@SignUpActivity,
//                        "wrong email or password!",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this@SignUpActivity,"Error: $it",Toast.LENGTH_SHORT).show()
            }
    }

    private fun SignUp() {
         email = mBinding.singUpInputEmail.text.toString().trim()
        val password = mBinding.singUpInputPassword.text.toString().trim()
        val confirmPass = mBinding.singUpInputConfirmPassword.text.toString().trim()
        val userName = mBinding.singUpInputUsername.text.toString().trim()
        if (password != confirmPass) {
            Toast.makeText(this@SignUpActivity, "Password did not match!", Toast.LENGTH_SHORT).show()
            return
        }
        mBinding.circularProgressBarSignUp.visibility=View.VISIBLE
        FirebaseUtil.firebaseAuth().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.isComplete) {
                        val id=FirebaseUtil.currentUserId()
                        val user = User(userName, "", id,"")
                        FirebaseUtil.currentUserDetails()
                            .set(user)
                            .addOnSuccessListener {
                                mBinding.flipper.displayedChild=2
                                Toast.makeText(this@SignUpActivity, "Account created", Toast.LENGTH_LONG).show()
                            }.addOnFailureListener {
                                Toast.makeText(this@SignUpActivity, "Account wasn't created", Toast.LENGTH_LONG).show()
                                mBinding.circularProgressBarSignUp.visibility=View.INVISIBLE
                            }

                }} else{
                    Toast.makeText(this@SignUpActivity, "Authentication failed!", Toast.LENGTH_SHORT).show()
                    mBinding.circularProgressBarSignUp.visibility=View.INVISIBLE
            }
    }
    }

    private fun showNextAnimation() {
        mBinding.flipper.setOutAnimation(this, android.R.anim.slide_in_left)
        mBinding.flipper.setOutAnimation(this, android.R.anim.slide_out_right)
    }

    private fun showPreviousAnimation() {
        mBinding.flipper.setInAnimation(this, R.anim.slide_in_right)
        mBinding.flipper.setOutAnimation(this, R.anim.slide_out_left)
    }

    private fun launchMainActivity(){
        if(FirebaseUtil.firebaseAuth().currentUser==null) {
            Toast.makeText(this@SignUpActivity,"You need to sign in",Toast.LENGTH_SHORT).show()
            return
        }
        val intent= Intent(this@SignUpActivity,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

//    override fun onAuthStateChanged(p0: FirebaseAuth) {
//        if(p0.currentUser!=null){
//            launchMainActivity()
//        }
//    }


}