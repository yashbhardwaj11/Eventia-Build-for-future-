package com.devinfusion.eventiafinal.adapter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.devinfusion.eventiafinal.MainActivity
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.dao.EventDao
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.fragment.QrGeneratorFragment
import com.devinfusion.eventiafinal.model.Event
import com.devinfusion.eventiafinal.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EventAdapter(private val context : Context, private val eventList : ArrayList<Event>,
                   private val navController: NavController
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val eventImage : ImageView = view.findViewById(R.id.eventImage)
        val eventHead : TextView = view.findViewById(R.id.eventHeading)
        val eventStartDate : TextView = view.findViewById(R.id.eventDate)
        val registerBt : Button = view.findViewById(R.id.eventBt)
        val card : CardView = view.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.events_item,parent,false)

        return EventViewHolder(view)
    }

    override fun getItemCount(): Int = eventList.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
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
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_eventDescriptionFragment,bundle)
        }

        GlobalScope.launch {
            val currentUser = userDao.getUser(user).await().getValue(User::class.java)
            if (currentUser?.eventsAttending?.contains(current.eventId) == true || currentUser?.eventVisited?.contains(current.eventId) == true){
                withContext(Dispatchers.Main){
                    holder.registerBt.isEnabled = false

                }
            }
            withContext(Dispatchers.Main){
                holder.registerBt.setOnClickListener {
                    Toast.makeText(context, "You have successfully registered ${current.eventHeading}", Toast.LENGTH_SHORT).show()
                    val eventDao = EventDao()
                    eventDao.addUserToEvent(user,current.eventId.toString())
                    createNotification(current.eventId.toString(),currentUser!!.userName.toString(),current.eventHeading.toString())
                }
            }
        }

    }
    private fun createNotification(eventId: String, userName: String, eventHeading: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channelId", "channelName", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", QrGeneratorFragment::class.java.name)
            putExtra("eventId", eventId)

        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val builder = NotificationCompat.Builder(context, "channelId")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Here are your tickets!")
            .setContentText("Hey! $userName Thank you registering for $eventHeading. Your tickets are available now")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        notificationManager.notify(1, builder.build())
    }
}