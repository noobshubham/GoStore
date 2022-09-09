package com.noobshubham.gostore.registration

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.noobshubham.gostore.MapsActivity
import com.noobshubham.gostore.R
import com.noobshubham.gostore.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private val REQ_ONE_TAP: Int = 300
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSignUp.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_signupFragment) }

        oneTapClient = Identity.getSignInClient(requireActivity())
        // initiate the auth variable
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
                            updateUI()
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
        updateUI()
    }

    private fun updateUI() {
        if (auth.currentUser != null) {
            requireActivity().startActivity(Intent(context, MapsActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}