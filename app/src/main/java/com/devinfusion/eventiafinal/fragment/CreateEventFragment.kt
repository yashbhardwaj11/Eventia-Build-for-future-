package com.devinfusion.eventiafinal.fragment

import android.media.MediaSyncEvent.createEvent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.dao.EventDao
import com.devinfusion.eventiafinal.databinding.FragmentCreateEventBinding
import com.devinfusion.eventiafinal.model.Event
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class CreateEventFragment : Fragment() {
    private var _binding : FragmentCreateEventBinding? = null
    private val binding get() = _binding!!
    private var imageUri : Uri? = null
    private lateinit var storageRef : StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateEventBinding.inflate(layoutInflater,container,false)
        storageRef =FirebaseStorage.getInstance().reference.child("EventPoster")

        val args = arguments
        val eventId = args?.getString("eventId")
        if (eventId!=null){
            editTheEvent(eventId)
        }

        binding.createEventBt.setOnClickListener {
            if (imageUri!=null){
                if (binding.eventNameEt.text!!.isNotEmpty()&&binding.eventDescriptionEt.text!!.isNotEmpty()&&
                    binding.eventLocationEt.text!!.isNotEmpty()&&binding.eventDateEt.text!!.isNotEmpty()&&binding.eventTimeEt.text!!.isNotEmpty()
                    && binding.eventLocationLinkEt.text!!.isNotEmpty()){
                    createEvent()
                }
                else{
                    Toast.makeText(requireContext(), "Fields should not be empty", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(requireContext(), "Please select a poster", Toast.LENGTH_SHORT).show()
            }
        }
        binding.eventPosterIV.setOnClickListener {
            launcher.launch("image/*")
        }

        return binding.root
    }

    private fun editTheEvent(eventId: String) {
        val eventDao = EventDao()
        GlobalScope.launch(Dispatchers.IO){
            val event = eventDao.getEvent(eventId).await().getValue(Event::class.java)
            withContext(Dispatchers.Main){
                binding.eventPosterIV.load(event!!.eventImage)
                binding.eventNameEt.setText(event.eventHeading)
                binding.eventDescriptionEt.setText(event.eventDescription)
                binding.eventLocationEt.setText(event.eventLocation)
                binding.eventLocationLinkEt.setText(event.eventLocationLink)
                binding.eventDateEt.setText(event.eventDate)
                binding.eventTimeEt.setText(event.eventTime)
            }
            binding.createEventBt.setOnClickListener {
                val eventChanged = Event(eventId,event!!.eventImage,binding.eventNameEt.text.toString(),
                    binding.eventDateEt.text.toString(),binding.eventDescriptionEt.text.toString(),event.eventOwner
                    ,binding.eventLocationEt.text.toString(),binding.eventLocationLinkEt.text.toString(),binding.eventTimeEt.text.toString())
                eventDao.eventCollection.child(eventId).setValue(eventChanged).addOnCompleteListener {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.createEventBt.visibility = View.VISIBLE
                    binding.eventNameEt.setText("")
                    binding.eventDateEt.setText("")
                    binding.eventLocationEt.setText("")
                    binding.eventLocationLinkEt.setText("")
                    binding.eventTimeEt.setText("")
                    binding.eventDescriptionEt.setText("")
                    binding.eventPosterIV.setImageResource(R.drawable.placeholder)
                    showSnackbarEdit()
                }
                    .addOnFailureListener {
                        if (isAdded){
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri->
        imageUri = uri
        binding.eventPosterIV.setImageURI(uri)
    }

    private fun createEvent(){
        binding.createEventBt.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.VISIBLE
        storageRef =storageRef.child(System.currentTimeMillis().toString())
        imageUri?.let {
            storageRef.putFile(it).addOnCompleteListener{task->
                if (task.isSuccessful){
                    storageRef.downloadUrl.addOnSuccessListener { uri->
                        GlobalScope.launch {
                            val auth = FirebaseAuth.getInstance()
                            val userEmail = auth.currentUser!!.email!!.trim().substringBefore("@")
                            val eventDao = EventDao()
                            val eventId = eventDao.eventCollection.push().key.toString()
                            val event = Event(eventId,uri.toString(),binding.eventNameEt.text.toString(),
                                binding.eventDateEt.text.toString(),binding.eventDescriptionEt.text.toString(),userEmail
                                ,binding.eventLocationEt.text.toString(),binding.eventLocationLinkEt.text.toString(),binding.eventTimeEt.text.toString())
                            if (isAdded){
                                eventDao.eventCollection.child(eventId).setValue(event).addOnCompleteListener {
                                    binding.progressBar.visibility = View.INVISIBLE
                                    binding.createEventBt.visibility = View.VISIBLE
                                    binding.eventNameEt.setText("")
                                    binding.eventDateEt.setText("")
                                    binding.eventLocationEt.setText("")
                                    binding.eventLocationLinkEt.setText("")
                                    binding.eventTimeEt.setText("")
                                    binding.eventDescriptionEt.setText("")
                                    binding.eventPosterIV.setImageResource(R.drawable.placeholder)
                                    showSnackbarCreate()

                                }
                            }
                        }
                    }
                }
                else{
                    Toast.makeText(requireContext(), "${task.exception}", Toast.LENGTH_SHORT).show()
                    binding.createEventBt.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun showSnackbarCreate() {
        val snackbar = Snackbar.make(binding.root, "Event created successfully", Snackbar.LENGTH_SHORT)
        snackbar.setAction("Dismiss") {
            snackbar.dismiss()
        }
        snackbar.show()
    }
    private fun showSnackbarEdit() {
        val snackbar = Snackbar.make(binding.root, "Event edited successfully", Snackbar.LENGTH_SHORT)
        snackbar.setAction("Dismiss") {
            snackbar.dismiss()
        }
        snackbar.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}