package com.devinfusion.eventiafinal.dao

import com.devinfusion.eventiafinal.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    private val database = Firebase.database
    val userCollection = database.getReference("User")

    @OptIn(DelicateCoroutinesApi::class)
    fun addUser(user : User?){
        user?.let {
            GlobalScope.launch(Dispatchers.IO) {
                userCollection.child(user.email!!).setValue(user)
            }
        }
    }

    fun getUser(uid : String) : Task<DataSnapshot> {
        return userCollection.child(uid).get()
    }

}