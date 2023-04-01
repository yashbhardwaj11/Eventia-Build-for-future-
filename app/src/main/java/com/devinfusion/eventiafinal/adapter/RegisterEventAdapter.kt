package com.devinfusion.eventiafinal.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.model.Event


class RegisteredEventsAdapter(val context : Context, val eventsList : ArrayList<Event>) : RecyclerView.Adapter<RegisteredEventsAdapter.RegisteredEventsViewHolder>(){

    inner class RegisteredEventsViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val eventHeading : TextView = view.findViewById(R.id.eventHeading)
        val eventDate : TextView = view.findViewById(R.id.eventDate)
        val eventImage : ImageView = view.findViewById(R.id.eventImage)
        val eventBt : Button = view.findViewById(R.id.eventBt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegisteredEventsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.registerd_event_list,parent,false)

        return RegisteredEventsViewHolder(view)
    }

    override fun getItemCount(): Int = eventsList.size

    override fun onBindViewHolder(holder: RegisteredEventsViewHolder, position: Int) {
        val currentEvent = eventsList[position]
        holder.eventHeading.text = currentEvent.eventHeading
        holder.eventDate.text = currentEvent.eventDate
        holder.eventBt.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("eventId", currentEvent.eventId)
            Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_qrGeneratorFragment,bundle)
        }
        holder.eventImage.load(currentEvent.eventImage){
            transformations(RoundedCornersTransformation(30f))
        }
    }
}