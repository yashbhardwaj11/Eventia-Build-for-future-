package com.devinfusion.eventiafinal.model

data class Event(val eventId : String? = "",
                 val eventImage : String? = "",
                 val eventHeading : String? = "",
                 val eventDate : String? = "",
                 val eventDescription : String? = "",
                 val eventOwner : String? = "",
                 val eventLocation : String? ="" ,
                 val eventLocationLink : String? ="" ,
                 val eventTime : String? ="",
                 val peopleAttending : ArrayList<String>? = arrayListOf(),
                 val peopleVerified : ArrayList<String>? = arrayListOf(),
                 val live : Boolean? = false
)