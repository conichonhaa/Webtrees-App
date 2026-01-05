package com.votredomaine.webtrees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import android.text.Editable
import android.text.TextWatcher

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
            // Enlever le https:// pour l'affichage
            val displayUrl = it.removePrefix("https://").removePrefix("http://")
            urlInput.setText(displayUrl)
        }

        // Ajouter un TextWatcher pour nettoyer automatiquement
        urlInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Enlever automatiquement http:// ou https:// si l'utilisateur les tape
                val text = s.toString()
                if (text.startsWith("https://") || text.startsWith("http://")) {
                    urlInput.removeTextChangedListener(this)
                    val cleaned = text.removePrefix("https://").removePrefix("http://")
                    urlInput.setText(cleaned)
                    urlInput.setSelection(cleaned.length)
                    urlInput.addTextChangedListener(this)
                }
            }
        })
    }

    fun getUrl(): String {
        val input = urlInput.text.toString().trim()

        // Nettoyer l'URL
        var cleaned = input
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')

        // Ajouter automatiquement https://
        return "https://$cleaned"
    }

    fun validateUrl(): Boolean {
        val input = urlInput.text.toString().trim()

        if (input.isEmpty()) {
            urlInput.error = getString(R.string.error_empty_url)
            return false
        }

        // Vérifier que c'est un nom de domaine valide
        val cleaned = input
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')

        if (!cleaned.contains(".")) {
            urlInput.error = "URL invalide. Exemple: genealogie.monsite.com"
            return false
        }

        urlInput.error = null
        return true
    }
}