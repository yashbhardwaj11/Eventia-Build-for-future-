package com.devinfusion.eventiafinal.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.dao.EventDao
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.model.Event
import com.devinfusion.eventiafinal.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EventReviewAdapter(private val context : Context, private val eventList : ArrayList<Event>)  : RecyclerView.Adapter<EventReviewAdapter.EventReviewViewHolder>(){

    inner class EventReviewViewHolder(view : View) : ViewHolder(view){
        val eventImage : ImageView = view.findViewById(R.id.eventImage)
        val eventHead : TextView = view.findViewById(R.id.eventHeading)
        val eventStartDate : TextView = view.findViewById(R.id.eventDate)
        val registerBt : Button = view.findViewById(R.id.eventBt)
        val card : CardView = view.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_review_item,parent,false)

        return EventReviewViewHolder(view)
    }

    override fun getItemCount(): Int  = eventList.size

    override fun onBindViewHolder(holder: EventReviewViewHolder, position: Int) {

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser!!.email!!.trim().substringBefore("@")
        val userDao = UserDao()
        val current = eventList[position]
        holder.eventStartDate.text = current.eventDate
        holder.eventHead.text = current.eventHeading
        holder.eventImage.load(current.eventImage){
            crossfade(true)
            placeholder(R.drawable.placeholder)
        }

        holder.card.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("eventId", current.eventId)
            bundle.putString("editIt","yes")
            Navigation.findNavController(it).navigate(R.id.action_eventsInReviewFragment_to_eventDescriptionFragment,bundle)
        }

        holder.registerBt.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("eventId",current.eventId)
            Navigation.findNavController(it).navigate(R.id.action_eventsInReviewFragment_to_createEventFragment,bundle)
        }


    }
}