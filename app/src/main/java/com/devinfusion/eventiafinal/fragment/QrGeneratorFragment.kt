package com.devinfusion.eventiafinal.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import coil.load
import coil.transform.RoundedCornersTransformation
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.dao.EventDao
import com.devinfusion.eventiafinal.dao.QrDao
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.databinding.FragmentQrGeneratorBinding
import com.devinfusion.eventiafinal.model.Event
import com.devinfusion.eventiafinal.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class QrGeneratorFragment : Fragment() {
    private var _binding : FragmentQrGeneratorBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var userDao : UserDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQrGeneratorBinding.inflate(layoutInflater,container,false)

        _binding = FragmentQrGeneratorBinding.inflate(layoutInflater,container,false)
        auth = FirebaseAuth.getInstance()
        userDao = UserDao()

        val args = arguments
        val eventId = args?.getString("eventId")
        val notification = args?.getString("notification")

        if (notification?.equals("true") == true){
            binding.backBt.visibility = View.INVISIBLE
        }

        binding.backBt.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_qrGeneratorFragment_to_profileFragment)
        }

        getQrCode(eventId)

        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getQrCode(eventId: String?) {
        binding.progressBar.visibility = View.VISIBLE
        GlobalScope.launch(Dispatchers.IO) {

            val eventDao = EventDao()
            val event = eventDao.getEvent(eventId!!).await().getValue(Event::class.java)
            val userMail = auth.currentUser!!.email!!.trim().substringBefore("@")
            val user = userDao.getUser(userMail).await().getValue(User::class.java)
            val finalDetails = User(user!!.uid,user.userEmail,user.email,user.userName,user.userPhotoUrl,user.collegeName,user.phoneNumber,
                user.course,user.location,user.eventsAttending,eventId)
            val qrDao = QrDao()
            val qrBitmap = qrDao.generateQrCode(finalDetails)
            withContext(Dispatchers.Main){
                binding.progressBar.visibility = View.INVISIBLE
                binding.card.visibility = View.VISIBLE
                binding.eventTicket.setImageBitmap(qrBitmap)
                binding.eventDate.text = event!!.eventDate
                binding.eventTime.text = event.eventTime
                binding.eventVenue.text = event.eventLocation
                binding.eventHeading.text = event.eventHeading
                binding.eventImage.load(event.eventImage){
                    transformations(RoundedCornersTransformation(30f))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}