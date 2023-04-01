package com.devinfusion.eventiafinal.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import coil.load
import coil.transform.CircleCropTransformation
import com.devinfusion.eventiafinal.activity.LoginActivity
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.databinding.FragmentBookedBinding
import com.devinfusion.eventiafinal.databinding.FragmentProfileBinding
import com.devinfusion.eventiafinal.model.Event
import com.devinfusion.eventiafinal.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private var _binding : FragmentProfileBinding? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var userDao : UserDao
    private lateinit var eventList : ArrayList<Event>
    private val binding get() = _binding!!
    private lateinit var adapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        auth = FirebaseAuth.getInstance()
        userDao = UserDao()
        eventList = arrayListOf()

        adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(BookedFragment(), "View Tickets")
        adapter.addFragment(MyEventsFragment(), "My Events")
        binding.tabViewpager.adapter = adapter
        binding.tabTablayout.setupWithViewPager(binding.tabViewpager)

        lifecycleScope.launch(Dispatchers.IO) {
            val user = auth.currentUser!!.email!!.trim().substringBefore("@")
            getUserDetails(user)
        }

        binding.exitBt.setOnClickListener {
            if (isAdded) {
                val i = Intent(requireContext(), LoginActivity::class.java)
                auth.signOut()
                startActivity(i)
                requireActivity().finish()
            }
        }

        return binding.root
    }

    private fun getUserDetails(user: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userDetails = userDao.getUser(user).await().getValue(User::class.java)
            withContext(Dispatchers.Main){
                binding.userImage.load(userDetails?.userPhotoUrl){
                    transformations(CircleCropTransformation())
                }
                binding.userName.text = userDetails?.userName
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ViewPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList: ArrayList<Fragment> = ArrayList()
        private val fragmentTitleList: ArrayList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }
}
