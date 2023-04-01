package com.devinfusion.eventiafinal.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import coil.load
import coil.transform.CircleCropTransformation
import com.devinfusion.eventiafinal.MainActivity
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.databinding.ActivityKnowAboutUserBinding
import com.devinfusion.eventiafinal.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KnowAboutUser : AppCompatActivity() {
    private lateinit var binding : ActivityKnowAboutUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowAboutUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = intent.getStringExtra("uid")
        val userName = intent.getStringExtra("userName")
        val userEmail = intent.getStringExtra("userEmail")
        val photo = intent.getStringExtra("userPhotoUrl")
        val email = intent.getStringExtra("email")

        bindData(userName,userEmail,photo.toString())
        binding.createUserBt.setOnClickListener {
            if (binding.userNameEt.text.toString().isNotEmpty() && binding.userCollegeEt.text.toString().isNotEmpty() &&
                binding.userPhoneNumberEt.text.toString().isNotEmpty() && binding.userCourseEt.text.toString().isNotEmpty() &&
                binding.userLocationEt.text.toString().isNotEmpty()){

                val collegeName = binding.userCollegeEt.text.toString()
                val course = binding.userCourseEt.text.toString()
                val phoneNumber = binding.userPhoneNumberEt.text.toString()
                val location = binding.userLocationEt.text.toString()

                val user = User(uid,userEmail,email,userName,photo
                    ,collegeName,phoneNumber,course,location)

                binding.progressBar.visibility = View.VISIBLE
                binding.createUserBt.visibility = View.INVISIBLE
                createUser(user)
            }else{
                Toast.makeText(this, "Empty Fields Are Not Allowed", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun bindData(userName: String?, userEmail: String?, photo: String) {
        binding.userNameEt.setText(userName)
        binding.userEmailEt.setText(userEmail)
        binding.userImage.load(photo){
            transformations(CircleCropTransformation())
        }
        binding.userEmailEt.isEnabled = false
    }

    private fun createUser(user: User) {
        GlobalScope.launch(Dispatchers.IO) {
            val userDao = UserDao()
            userDao.addUser(user)
            withContext(Dispatchers.Main){
                val i =  Intent(this@KnowAboutUser, MainActivity::class.java)
                startActivity(i)
                finish()
            }
        }
    }
}