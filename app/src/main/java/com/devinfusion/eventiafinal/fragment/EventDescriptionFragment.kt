package com.devinfusion.eventiafinal.fragment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.navigation.Navigation
import coil.load
import coil.transform.CircleCropTransformation
import com.devinfusion.eventiafinal.MainActivity
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.dao.EventDao
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.databinding.FragmentEventDescriptionBinding
import com.devinfusion.eventiafinal.model.Event
import com.devinfusion.eventiafinal.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class EventDescriptionFragment : Fragment() {
    private var _binding : FragmentEventDescriptionBinding? = null
    private val binding get() = _binding!!
    private lateinit var context: Context
    private var editIt : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventDescriptionBinding.inflate(layoutInflater,container,false)

        context = binding.root.context
        val args = arguments
        val eventId = args?.getString("eventId")

        editIt = args?.getString("editIt")

        getDetails(eventId)

        return binding.root
    }

    private fun getDetails(eventId: String?) {
        val eventDao = EventDao()
        GlobalScope.launch (Dispatchers.IO){
            val event = eventDao.getEvent(eventId!!).await().getValue(Event::class.java)
            withContext(Dispatchers.Main){
                bindData(event)
            }
        }
    }

    private fun bindData(event: Event?) {
        binding.cardView.visibility = View.VISIBLE
        binding.backBt.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_eventDescriptionFragment_to_homeFragment)
        }
        binding.eventHeading.text =event!!.eventHeading
        binding.eventDate.text =event!!.eventDate
        binding.eventLocation.text =event.eventLocation
        binding.eventTime.text = event.eventTime
        binding.eventImage.load(event.eventImage)
        val people = event.peopleAttending?.size
        val peopleVerified = event.peopleVerified?.size
        val totalParticipant = people!!+peopleVerified!!
        binding.peopleAttending.text = "${totalParticipant.toString()} Participate"
        binding.image1.load("https://images.unsplash.com/photo-1521038199265-bc482db0f923?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80"){
            transformations(CircleCropTransformation())
        }
        binding.image2.load("https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80"){
            transformations(CircleCropTransformation())
        }
        binding.image3.load("https://images.unsplash.com/photo-1611880147493-7542bdb0f024?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1033&q=80"){
            transformations(CircleCropTransformation())
        }
        binding.eventDescription.text = event.eventDescription
        val eventDao = EventDao()
        val auth = FirebaseAuth.getInstance()
        val userEmail = auth.currentUser!!.email!!.trim().toString().substringBefore("@")


        if (editIt.equals("yes")){
            binding.bookNowBt.isEnabled = true
            binding.bookNowBt.setText("Edit")
            binding.bookNowBt.isEnabled = true
            binding.bookNowBt.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("eventId",event.eventId)
                Navigation.findNavController(it).navigate(R.id.action_eventDescriptionFragment_to_createEventFragment,bundle)
            }
        }else{
            if (event.peopleAttending?.contains(userEmail) == true){
                binding.bookNowBt.isEnabled = false
            }else{
                binding.bookNowBt.setOnClickListener {
                    GlobalScope.launch (Dispatchers.IO){
                        val userDao = UserDao()
                        val user = userDao.getUser(userEmail).await().getValue(User::class.java)
                        eventDao.addUserToEvent(userEmail, event.eventId!!)
                        createNotification(event.eventId,user!!.userName.toString(),event.eventHeading.toString())
                    }
                }
            }
        }

        binding.mapsBt.setOnClickListener {
            val url = event.eventLocationLink?.trim()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun createNotification(eventId: String, userName: String, eventHeading: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("TicketsInfo", "Tickets", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", QrGeneratorFragment::class.java.name)
            putExtra("eventId", eventId)

        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val builder = NotificationCompat.Builder(context, "TicketsInfo")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Here are your tickets!")
            .setContentText("Hey! $userName Thank you registering for $eventHeading. Your tickets are available now")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        notificationManager.notify(1, builder.build())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}