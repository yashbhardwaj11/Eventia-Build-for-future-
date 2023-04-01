package com.devinfusion.eventiafinal.dao

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.model.Elements
import com.devinfusion.eventiafinal.model.Event
import com.devinfusion.eventiafinal.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EventDao {
    private val database = Firebase.database
    val eventCollection = database.getReference("Events")

    fun addEvent(event : Event?, content: Context) {
        event?.let {
            GlobalScope.launch(Dispatchers.IO) {
                eventCollection.push().setValue(event)
            }
        }
    }

    fun getEvent(eventId : String) : Task<DataSnapshot> {
        return eventCollection.child(eventId).get()
    }

    fun addUserToEvent(uid : String,eventId: String){
        val userDao = UserDao()
        GlobalScope.launch(Dispatchers.IO) {
            val user = userDao.getUser(uid).await().getValue(User::class.java)
            val event = getEvent(eventId).await().getValue(Event::class.java)

            if (user?.eventsAttending?.contains(eventId) == true){

            }
            else{
                event?.peopleAttending?.add(uid)
                user?.eventsAttending?.add(eventId)
            }
            eventCollection.child(eventId).setValue(event)
            userDao.addUser(user)
        }
    }

    fun userVerified(email: String, eventId: String, context: Context,elements : Elements){
        GlobalScope.launch(Dispatchers.IO) {
            val event = getEvent(eventId).await().getValue(Event::class.java)
            val auth = FirebaseAuth.getInstance()
            val userMail = auth.currentUser!!.email!!.trim().substringBefore("@")
            val userDao = UserDao()
            val user = userDao.getUser(email).await().getValue(User::class.java)
            if (event != null){
                if (event.eventOwner == userMail){
                    if ( event.peopleAttending?.contains(email) == true) {
                        event.peopleAttending.remove(email)
                        event.peopleVerified?.add(email)
                        user!!.eventsAttending?.remove(eventId)
                        userDao.userCollection.child(userMail).setValue(user).addOnCompleteListener {
                            eventCollection.child(eventId).setValue(event).addOnSuccessListener {
//                                Toast.makeText(context, "User Verified", Toast.LENGTH_LONG).show()
//                                val view = View(context)
//                                view.setBackgroundResource(android.R.color.holo_green_light)
//                                elements.userName.background = view.background
//                                elements.userCollege.background = view.background
//                                elements.userMail.background = view.background
//                                elements.eventName.background = view.background
                            }.addOnFailureListener {
                                val view = View(context)
                                view.setBackgroundResource(android.R.color.holo_red_light)
                                elements.userName.background = view.background
                                elements.userCollege.background = view.background
                                elements.userMail.background = view.background
                                elements.eventName.background = view.background
//                                Toast.makeText(context, "Something went wrong ${it.message}", Toast.LENGTH_LONG).show()
                            }
                        }.addOnFailureListener {
                            val view = View(context)
                            view.setBackgroundResource(android.R.color.holo_red_light)
                            elements.userName.background = view.background
                            elements.userCollege.background = view.background
                            elements.userMail.background = view.background
                            elements.eventName.background = view.background
//                            Toast.makeText(context, "something went wrong with user ${it.message}", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else{
                        withContext(Dispatchers.Main){
                            val view = View(context)
                            view.setBackgroundResource(android.R.color.holo_red_light)
                            elements.userName.background = view.background
                            elements.userCollege.background = view.background
                            elements.userMail.background = view.background
                            elements.eventName.background = view.background
//                            Toast.makeText(context, "User Does not exist", Toast.LENGTH_SHORT).show()

                        }
                    }
                }
            }
        }
    }

}