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
import com.devinfusion.eventiafinal.dao.EventDao
import com.devinfusion.eventiafinal.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileAdapter(val eventsList : ArrayList<String> , val context : Context) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {
    inner class ProfileViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val eventHeading : TextView = view.findViewById(R.id.eventHeading)
        val eventDate : TextView = view.findViewById(R.id.eventDate)
        val eventImage : ImageView = view.findViewById(R.id.eventImage)
        val eventBt : Button = view.findViewById(R.id.eventBt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.registerd_event_list,parent,false)

        return ProfileViewHolder(view)
    }

    override fun getItemCount(): Int = eventsList.size

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val currentEventId = eventsList[position]
        val eventDao = EventDao()
        GlobalScope.launch(Dispatchers.IO) {
            val event = eventDao.getEvent(currentEventId).await().getValue(Event::class.java)
            if (event!=null){
                withContext(Dispatchers.Main){
                    holder.eventHeading.text = event!!.eventHeading
                    holder.eventDate.text = event.eventDate
                    holder.eventImage.load(event.eventImage){
                        transformations(RoundedCornersTransformation(30f))
                    }
                    holder.eventBt.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putString("eventId", event.eventId)
                        Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_qrGeneratorFragment,bundle)
                    }
                }
            }
        }
    }
}