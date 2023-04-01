package com.devinfusion.eventiafinal.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.devinfusion.eventiafinal.MainActivity
import com.devinfusion.eventiafinal.R
import com.devinfusion.eventiafinal.dao.UserDao
import com.devinfusion.eventiafinal.databinding.ActivityLoginBinding
import com.devinfusion.eventiafinal.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)


        binding.signInBT.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleTask(task)
        }
    }

    private fun handleTask(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount = task.result
            if (account!=null){
                firebaseAuthWithGoogle(account)
            }
        }
        else{
            Toast.makeText(this@LoginActivity, "Failed to Sign In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        binding.progressBar.visibility = View.VISIBLE
        binding.signInBT.visibility = View.INVISIBLE
        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Dispatchers.Main){
                updateUI(firebaseUser)
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if (firebaseUser!=null){
            val email = firebaseUser.email!!.trim().substringBefore("@")
            val user = User(firebaseUser.uid,firebaseUser.email,email,firebaseUser.displayName,firebaseUser.photoUrl.toString())
            checkUserExist(user)
        }
        else{
            binding.progressBar.visibility = View.GONE
            binding.signInBT.visibility = View.VISIBLE
        }

    }

    private fun checkUserExist(user : User){
        binding.progressBar.visibility = View.VISIBLE
        binding.signInBT.visibility = View.INVISIBLE
        val userDao = UserDao()
        userDao.userCollection.child(user.email!!).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    startActivity(Intent(this@LoginActivity , MainActivity::class.java))
                    finish()
                }
                else{
                    binding.signInBT.visibility = View.INVISIBLE
                    val intent = Intent(this@LoginActivity, KnowAboutUser::class.java)
                    intent.putExtra("uid",user.uid)
                    intent.putExtra("userName",user.userName)
                    intent.putExtra("userEmail",user.userEmail)
                    intent.putExtra("email",user.email)
                    intent.putExtra("userPhotoUrl",user.userPhotoUrl)
                    intent.putExtra("location",user.location)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        updateUI(user)
    }
}