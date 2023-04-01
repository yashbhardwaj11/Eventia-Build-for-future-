package com.devinfusion.eventiafinal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.devinfusion.eventiafinal.databinding.ActivityMainBinding
import com.devinfusion.eventiafinal.fragment.QrGeneratorFragment
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val destination = intent.getStringExtra("destination")
        val eventId = intent.getStringExtra("eventId")
        val navController = findNavController(R.id.myFragmentContainer)

        if (destination != null) {

            val fragment = when (destination) {
                QrGeneratorFragment::class.java.name -> {
                    val bundle = Bundle()
                    bundle.putString("eventId",eventId)
                    bundle.putString("notification","true")
                    val myFragment = QrGeneratorFragment()
                    myFragment.arguments = bundle
                    val fragmentManager = supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.myFragmentContainer, myFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }
                // Add additional cases for other destination fragments
                else -> null
            }

//            if (fragment != null) {
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.myFragmentContainer, fragment)
//                    .commit()
//            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener{
            if (!it.isSuccessful){
                Log.d("TokenFirebase","Something went wrong")
                return@addOnCompleteListener
            }
            val token : String = it.result
            Log.d("TokenFirebase",token)
        }

        NavigationUI.setupWithNavController(binding.bottomNav,navController)


    }
}