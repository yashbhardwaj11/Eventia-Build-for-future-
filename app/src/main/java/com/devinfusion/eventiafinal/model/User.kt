package com.devinfusion.eventiafinal.model

data class User(val uid : String? = "",
                val userEmail : String? = "",
                val email : String? = "",
                val userName : String? = "",
                val userPhotoUrl : String? = "",
                val collegeName : String? = "",
                val phoneNumber : String? = "",
                val course : String? = "",
                val location : String? = "",
                val eventsAttending : ArrayList<String>? = arrayListOf(),
                val eventRegisteredFor : String? = "",
                val eventVisited : ArrayList<String>? = arrayListOf()
)
