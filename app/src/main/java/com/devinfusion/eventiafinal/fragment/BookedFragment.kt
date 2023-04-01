package com.devinfusion.eventiafinal.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.adapter.ProfileAdapter
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.databinding.FragmentBookedBinding
import com.devinfusion.eventiafinal.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class BookedFragment : Fragment() {
    private var _binding : FragmentBookedBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRegisteredEventList : ArrayList<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookedBinding.inflate(layoutInflater,container,false)
        userRegisteredEventList = arrayListOf()

//        getRegisteredEvents()

        return binding.root
    }

    private fun getRegisteredEvents(){
        val userDao = UserDao()
        val auth = FirebaseAuth.getInstance()
        val userMail = auth.currentUser!!.email!!.trim().substringBefore("@")
        GlobalScope.launch(Dispatchers.IO) {
            val user = userDao.getUser(userMail).await().getValue(User::class.java)
            for (i in user?.eventsAttending!!){
                userRegisteredEventList.add(i)
            }
            withContext(Dispatchers.Main){
                binding.progressBar.visibility = View.INVISIBLE
                if (userRegisteredEventList.isEmpty()){
//                    binding.nothingToShowBt.visibility = View.VISIBLE
                    binding.nothingToShowTT.visibility = View.VISIBLE
                    binding.bookedRv.visibility = View.INVISIBLE
//                    binding.nothingToShowBt.setOnClickListener {
//                        Navigation.findNavController(it).navigate(R.id.action_profileFragment_to_homeFragment)
//                    }
                }
                else{
//                    binding.nothingToShowBt.visibility = View.INVISIBLE
                    binding.nothingToShowTT.visibility = View.INVISIBLE
                    binding.bookedRv.visibility = View.VISIBLE
                    binding.bookedRv.adapter = ProfileAdapter(userRegisteredEventList,requireContext())
                    binding.bookedRv.layoutManager = LinearLayoutManager(requireContext())
                    binding.bookedRv.hasFixedSize()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRegisteredEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding= null
    }


}