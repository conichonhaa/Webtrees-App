package com.votredomaine.webtrees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class SetupCredentialsFragment : Fragment() {

    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup_credentials, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        usernameInput = view.findViewById(R.id.usernameInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        
        // Charger les identifiants si déjà enregistrés
        val prefsManager = PreferencesManager(requireContext())
        prefsManager.getUsername()?.let {
            usernameInput.setText(it)
        }
        prefsManager.getPassword()?.let {
            passwordInput.setText(it)
        }
    }

    fun getUsername(): String {
        return usernameInput.text.toString().trim()
    }

    fun getPassword(): String {
        return passwordInput.text.toString().trim()
    }

    fun validateCredentials(): Boolean {
        val username = getUsername()
        val password = getPassword()
        
        var isValid = true
        
        if (username.isEmpty()) {
            usernameInput.error = getString(R.string.error_empty_username)
            isValid = false
        } else {
            usernameInput.error = null
        }
        
        if (password.isEmpty()) {
            passwordInput.error = getString(R.string.error_empty_password)
            isValid = false
        } else {
            passwordInput.error = null
        }
        
        return isValid
    }
}
