package com.noobshubham.gostore.registration

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.noobshubham.gostore.MapsActivity
import com.noobshubham.gostore.databinding.FragmentLoginBinding
import com.noobshubham.gostore.model.UserData

class LoginFragment : Fragment() {

    private val REQ_ONE_TAP: Int = 300
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // progressBar
    private lateinit var progressBar: ProgressBar

    // to verify firebase authentication
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient

    // on tapping the button google will save the credentials
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        progressBar = binding.progressBar
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSignUp.setOnClickListener { findNavController().navigate(com.noobshubham.gostore.R.id.action_loginFragment_to_signupFragment) }

        binding.btnLogin.setOnClickListener {
            val inputEmail = binding.tietEmail.text.toString().trim()
            val inputPassword = binding.tiePassword.text.toString().trim()

            val userData = UserData("${auth.currentUser}", inputEmail, inputPassword)

            if (validateInput(userData)) signIn(userData)
        }

        oneTapClient = Identity.getSignInClient(requireActivity())
        // initiate the auth variable
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(com.noobshubham.gostore.R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()

        // auth button click listener
        binding.googleButton.setOnClickListener { signInWithGoogle() }
    }

    private fun signIn(userData: UserData) {
        progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(userData.email, userData.password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(
                        context, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
                progressBar.visibility = View.GONE
            }
    }

    private fun validateInput(userData: UserData): Boolean {
        if (userData.email.isEmpty()) {
            binding.tietEmail.error = "Email is empty."
            return false
        }
        if (userData.password.isEmpty()) {
            binding.tiePassword.error = "Password is empty."
            return false
        }
        return true
    }

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Snackbar.make(binding.root, e.message.toString(), Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener(requireActivity()) { e ->
                Snackbar.make(binding.root, e.message.toString(), Snackbar.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
        val idToken = googleCredential.googleIdToken
        when {
            idToken != null -> {
                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            updateUI(auth.currentUser)
                        } else {
                            // If sign in fails, display a message to the user.
                            Snackbar.make(
                                binding.root,
                                task.exception?.message.toString(),
                                Snackbar.LENGTH_INDEFINITE
                            ).show()
                        }
                    }
            }
            else -> Snackbar.make(binding.root, "No Token Found!", Snackbar.LENGTH_SHORT).show()
        }
    }


    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        progressBar.visibility = View.GONE
        if (user != null) {
            requireActivity().startActivity(Intent(context, MapsActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}