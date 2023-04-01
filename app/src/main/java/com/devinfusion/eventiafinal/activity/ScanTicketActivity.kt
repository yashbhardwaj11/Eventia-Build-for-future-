package com.devinfusion.eventiafinal.activity

import android.Manifest
import android.R
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.devinfusion.eventiafinal.dao.EventDao
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.databinding.ActivityScanTicketBinding
import com.devinfusion.eventiafinal.model.Elements
import com.devinfusion.eventiafinal.model.Event
import com.devinfusion.eventiafinal.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject


class ScanTicketActivity : AppCompatActivity() {
    private lateinit var binding : ActivityScanTicketBinding
    private val CAMERA_PERMISSION_CODE = 100
    private var qrScanIntegrator: IntentIntegrator? = null
    private var eventRegisteredFor : String? = ""
    private var verifiedUser : String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        setOnClickListener()
        setupScanner()

    }


    private fun setupScanner() {
        qrScanIntegrator = IntentIntegrator(this)
        qrScanIntegrator?.setOrientationLocked(false)

    }


    private fun setOnClickListener() {
        binding.scanTicketBt.setOnClickListener { performAction() }

        binding.verifyUserBt.setOnClickListener {
            if (binding.eventNameEt.text.toString().isNotEmpty()){
                verifyTheUser()
            }
            else{
                showSnackbar(this@ScanTicketActivity,"scan the ticket before verifying")
            }
        }
    }

    private fun performAction() {
        qrScanIntegrator?.initiateScan()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, getString(com.devinfusion.eventiafinal.R.string.result_not_found), Toast.LENGTH_LONG).show()
            } else {
                try {
                    val obj = JSONObject(result.contents)

                    Log.d("VolleyTag" , obj.toString())

                    binding.userNameEt.setText(obj.getString("userName"))
                    binding.userCollegeEt.setText(obj.getString("collegeName"))
                    binding.userMailEt.setText(obj.getString("userEmail"))
                    val eventId = obj.getString("eventRegisteredFor")
                    GlobalScope.launch(Dispatchers.IO) {
                        val eventDao = EventDao()
                        val event = eventDao.getEvent(eventId).await().getValue(Event::class.java)
                        withContext(Dispatchers.Main){
                            binding.eventNameEt.setText(event!!.eventHeading)
                        }
                    }
                    eventRegisteredFor = obj.getString("eventRegisteredFor")
                    verifiedUser = obj.getString("email")


                } catch (e: JSONException) {
                    e.printStackTrace()
                    val view = View(this@ScanTicketActivity)
                    view.setBackgroundResource(android.R.color.holo_red_light)
                    binding.userNameEtField.background = view.background
                    binding.userCollegeEtField.background = view.background
                    binding.userMailEtField.background = view.background
                    binding.eventNameEtField.background = view.background

//                    Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()
                    showSnackbar(this,result.contents)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //  eventDao.userVerified(verifiedUser!!,eventRegisteredFor!!,this@ScanTicketActivity,elements)

    private fun verifyTheUser() {
        GlobalScope.launch {
            val eventDao = EventDao()
            verifyUser(verifiedUser!!,eventRegisteredFor!!)
        }
    }


    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@ScanTicketActivity, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this@ScanTicketActivity, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackbar(this,"camera permission granted")
            } else {
                checkPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE)
            }
        }
    }

    fun verifyUser(email : String,eventId : String){
        val auth = FirebaseAuth.getInstance()
        val eventDao = EventDao()
        GlobalScope.launch(Dispatchers.IO) {
            val event = eventDao.getEvent(eventId).await().getValue(Event::class.java)
            val userMail = auth.currentUser!!.email!!.trim().substringBefore("@")
            val userDao = UserDao()
            val user = userDao.getUser(email).await().getValue(User::class.java)
            if (event!=null){
                if (event.eventOwner == userMail){
                    if (event.peopleAttending?.contains(email) == true){
                        if (event.peopleVerified?.contains(email) == false){
                            event.peopleAttending.remove(email)
                            event.peopleVerified.add(email)
                            val handler = Handler(Looper.getMainLooper())
                            handler.post {
                                binding.finalTT.text = "User Verified"
                                binding.finalTT.setTextColor(Color.GREEN)
                            }
                        }
                        else{
                            val handler = Handler(Looper.getMainLooper())
                            handler.post {
                                binding.finalTT.text = "Already Verified"
                                binding.finalTT.setTextColor(Color.RED)
                            }
                        }
                        eventDao.eventCollection.child(eventId).setValue(event).addOnCompleteListener {
                            showSnackbar(this@ScanTicketActivity, "Next User")
                        }.addOnFailureListener {
                            showSnackbar(this@ScanTicketActivity,"Something went wrong try again")
                        }
                    }
                    else{
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            binding.finalTT.text = "User Not Registered"
                            binding.finalTT.setTextColor(Color.RED)
                        }

                        showSnackbar(this@ScanTicketActivity,"User not registered ")
                    }
                }
                else{

                    binding.scanTicketBt.visibility = View.INVISIBLE
                    binding.verifyUserBt.visibility = View.INVISIBLE
                    showSnackbar(this@ScanTicketActivity,"must be a owner of the event")
                }
            }
        }
    }



    private fun showSnackbar(context : Context, message : String) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        snackbar.setAction("Dismiss") {
            snackbar.dismiss()
        }
        snackbar.show()
    }

}