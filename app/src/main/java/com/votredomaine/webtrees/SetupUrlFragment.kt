package com.votredomaine.webtrees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class SetupUrlFragment : Fragment() {

    private lateinit var urlInput: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup_url, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        urlInput = view.findViewById(R.id.urlInput)
        
        // Charger l'URL si déjà enregistrée
        val prefsManager = PreferencesManager(requireContext())
        prefsManager.getSiteUrl()?.let {
            urlInput.setText(it)
        }
    }

    fun getUrl(): String {
        return urlInput.text.toString().trim()
    }

    fun validateUrl(): Boolean {
        val url = getUrl()
        
        if (url.isEmpty()) {
            urlInput.error = getString(R.string.error_empty_url)
            return false
        }
        
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            urlInput.error = getString(R.string.error_invalid_url)
            return false
        }
        
        urlInput.error = null
        return true
    }
}
