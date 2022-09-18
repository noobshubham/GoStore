package com.noobshubham.gostore.registration

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.noobshubham.gostore.MapsActivity
import com.noobshubham.gostore.R
import com.noobshubham.gostore.databinding.FragmentLoginBinding
import com.noobshubham.gostore.model.UserData

const val TAG = "LoginFragment"
const val RC_ONE_TAP = 123

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // progressBar
    private lateinit var progressBar: ProgressBar

    // Firebase Auth
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    // Control whether user declined One Tap UI
    private var userDeclinedOneTap = false

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

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

        binding.tvSignUp.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_signupFragment) }

        binding.btnLogin.setOnClickListener {
            val inputEmail = binding.tietEmail.text.toString().trim()
            val inputPassword = binding.tiePassword.text.toString().trim()

            val userData = UserData("${auth.currentUser}", inputEmail, inputPassword)

            if (validateInput(userData)) signIn(userData)
        }

        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
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

    private fun updateUI(user: FirebaseUser?) {
        progressBar.visibility = View.GONE
        if (user != null) {
            requireActivity().startActivity(Intent(context, MapsActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, RC_ONE_TAP,
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

    private fun firebaseAuthWithGoogle(googleIdToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(
                        requireView(),
                        "Authentication Failed.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            // This credential contains a googleIdToken which
            // we can use to authenticate with FirebaseAuth
            credential.googleIdToken?.let {
                firebaseAuthWithGoogle(it)
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    // The user closed the dialog
                    // userDeclinedOneTap = true
                }
                CommonStatusCodes.NETWORK_ERROR -> {
                    // No Internet connection ?
                    Toast.makeText(context, "Internet Disconnected!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Some other error
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}