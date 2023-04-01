package com.devinfusion.eventiafinal.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import coil.load
import coil.transform.CircleCropTransformation
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.activity.ScanTicketActivity
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.databinding.FragmentAdminBinding
import com.devinfusion.eventiafinal.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdminFragment : Fragment() {
    private var _binding : FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminBinding.inflate(layoutInflater,container,false)
        auth = FirebaseAuth.getInstance()

        getUserDetails()

        binding.createEvent.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_adminFragment_to_createEventFragment)
        }
        if (isAdded){
            binding.scanTickets.setOnClickListener {
                startActivity(Intent(requireContext(),ScanTicketActivity::class.java))
            }
        }
        binding.eventsInReview.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_adminFragment_to_eventsInReviewFragment)
        }

        return binding.root
    }

    private fun getUserDetails() {
        GlobalScope.launch (Dispatchers.IO){
            val userMail = auth.currentUser!!.email.toString().trim().substringBefore("@")
            val userDao = UserDao()
            val user = userDao.getUser(userMail).await().getValue(User::class.java)
            withContext(Dispatchers.Main){
                if (userMail == "yash82206"){
                    binding.admin.visibility = View.VISIBLE
                }
                binding.userName.text = user!!.userName
                binding.userImage.load(user.userPhotoUrl){
                    transformations(CircleCropTransformation())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}