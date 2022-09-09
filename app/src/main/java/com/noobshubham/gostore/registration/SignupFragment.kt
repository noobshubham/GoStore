package com.noobshubham.gostore.registration

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.noobshubham.gostore.MapsActivity
import com.noobshubham.gostore.R
import com.noobshubham.gostore.databinding.FragmentSignupBinding
import com.noobshubham.gostore.model.UserData

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create Button
        binding.btnRegister.setOnClickListener {
            val userName = binding.tietUsername.text.toString().trim()
            val inputEmail = binding.tietEmail.text.toString().trim()
            val passWord = binding.tietPassword.text.toString().trim()

            val userData = UserData(userName, inputEmail, passWord)

            if (validateInput(userData)) registerUser(userData)
        }

        // SignIn Button
        binding.imageView.setOnClickListener { findNavController().navigate(R.id.action_signupFragment_to_loginFragment) }
    }

    private fun validateInput(userData: UserData): Boolean {
        if (userData.username.isEmpty()) {
            binding.tietUsername.error = "Username is empty."
            return false
        }
        if (userData.email.isEmpty()) {
            binding.tietEmail.error = "Email is empty."
            return false
        }
        if (userData.password.isEmpty()) {
            binding.tietPassword.error = "Password is empty."
            return false
        }
        return true
    }

    private fun registerUser(userData: UserData) {
        auth.createUserWithEmailAndPassword(userData.email, userData.password)
            .addOnSuccessListener {
                OnCompleteListener<AuthResult>() {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            context,
                            "You've been registered successfully.",
                            Toast.LENGTH_SHORT
                        ).show()
                        sendUserData(userData)
                        startActivity(Intent(context, MapsActivity::class.java))
                    } else
                        Toast.makeText(context, "Email already exists", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendUserData(userData: UserData) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("Users").document().set(userData).addOnSuccessListener { }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "it.message.toString()",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}