package com.noobshubham.gostore.registration

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.noobshubham.gostore.MapsActivity
import com.noobshubham.gostore.R
import com.noobshubham.gostore.databinding.FragmentSignupBinding
import com.noobshubham.gostore.model.UserData

class SignupFragment : Fragment() {

    // binding variables
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    // Firebase Auth Variables
    private lateinit var auth: FirebaseAuth

    // progressBar
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        progressBar = binding.progressBar
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

            if (validateInput(userData)) createUser(userData)
        }

        // SignIn Button
        binding.imageView.setOnClickListener { findNavController().navigate(R.id.action_signupFragment_to_loginFragment) }
    }

    private fun createUser(userData: UserData) {
        progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(userData.email, userData.password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(
                        context,
                        "You've been registered successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().startActivity(Intent(context, MapsActivity::class.java))
                    requireActivity().finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        context,
                        "Account Creation Failed, Try Again!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                progressBar.visibility = View.GONE
            }
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}