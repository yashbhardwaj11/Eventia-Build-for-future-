






package com.devinfusion.eventiafinal.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.adapter.EventAdapter
import com.devinfusion.eventiafinal.dao.EventDao
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.databinding.FragmentHomeBinding
import com.devinfusion.eventiafinal.model.Event
import com.devinfusion.eventiafinal.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var eventList : ArrayList<Event>
    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(layoutInflater,container,false)

        auth = FirebaseAuth.getInstance()
        eventList = arrayListOf()

        getUserDetails()

        binding.nearByEventBt.setOnClickListener {
            showSnackbar()
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            getData()
        }

        binding.userImage.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.notificationIv.setOnClickListener {
            showSnackbar()
        }


        return binding.root
    }

    private fun getUserDetails() {
        GlobalScope.launch (Dispatchers.IO){
            val userMail = auth.currentUser!!.email!!.trim().substringBefore("@")
            val userDao = UserDao()
            val user = userDao.getUser(userMail).await().getValue(User::class.java)
            withContext(Dispatchers.Main){
                binding.userImage.load(user?.userPhotoUrl){
                    transformations(CircleCropTransformation())
                }
                binding.userLocation.text = user?.location
                binding.userName.text = user?.userName
            }
        }
    }

    private fun showSnackbar() {
        val snackbar = Snackbar.make(binding.root, "This feature currently not available", Snackbar.LENGTH_SHORT)
        snackbar.setAction("Dismiss") {
            snackbar.dismiss()
        }
        snackbar.show()
    }

    private fun getData() {

        val eventDao = EventDao()
        val query = eventDao.eventCollection.orderByChild("live").equalTo(true)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()
                if (snapshot.exists()){
                    for (snap in snapshot.children){
                        val events = snap.getValue(Event::class.java)
                        eventList.add(events!!)
                    }
                    bindDataToRV(eventList)
                }
                else{
                    if (isAdded){
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.nothingToShowTT.visibility = View.VISIBLE
                    }
                    Log.d("Repo","Nothing to show")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                if (isAdded){
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.nothingToShowTT.visibility = View.VISIBLE
                }
                Log.d("Repo"," ${error.message}")
            }
        })

    }

    private fun bindDataToRV(eventList: java.util.ArrayList<Event>) {
        if (isAdded){
            binding.progressBar.visibility = View.INVISIBLE
            adapter = EventAdapter(binding.root.context,eventList,Navigation.findNavController(requireView()))
            binding.eventsRv.adapter = adapter
            binding.eventsRv.layoutManager = LinearLayoutManager(requireContext())
            binding.eventsRv.hasFixedSize()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}