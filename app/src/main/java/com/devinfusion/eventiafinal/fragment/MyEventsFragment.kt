package com.devinfusion.eventiafinal.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.adapter.EventReviewAdapter
import com.devinfusion.eventiafinal.adapter.MyEventsAdapter
import com.devinfusion.eventiafinal.dao.EventDao
import com.devinfusion.eventiafinal.databinding.FragmentMyEventsBinding
import com.devinfusion.eventiafinal.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MyEventsFragment : Fragment() {
    private var _binding : FragmentMyEventsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var eventList : ArrayList<Event>
    private lateinit var adapter: MyEventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyEventsBinding.inflate(layoutInflater,container,false)
        eventList = arrayListOf()
        auth = FirebaseAuth.getInstance()

//        getData()

        return binding.root
    }

    private fun getData() {
        val eventDao = EventDao()
        val currentUser = auth.currentUser!!.email.toString().trim().substringBefore("@")

        eventDao.eventCollection.orderByChild("eventOwner")
            .equalTo(currentUser)
            .addValueEventListener(object : ValueEventListener {
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
                            if (eventList.isEmpty()){
                                binding.progressBar.visibility = View.INVISIBLE
                                binding.nothingToShowTT.visibility = View.VISIBLE
                            }
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
        if (isAdded && _binding != null){
            binding.progressBar.visibility = View.INVISIBLE
            adapter = MyEventsAdapter(requireContext(),eventList)
            binding.myEventsRv.adapter = adapter
            binding.myEventsRv.layoutManager = LinearLayoutManager(requireContext())
            binding.myEventsRv.hasFixedSize()
        }
    }


    override fun onResume() {
        super.onResume()
//        getData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}