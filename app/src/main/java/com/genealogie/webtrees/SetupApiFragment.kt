package com.genealogie.webtrees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class SetupApiFragment : Fragment() {

    private lateinit var inputClientId: TextInputEditText
    private lateinit var inputClientSecret: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup_api, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputClientId     = view.findViewById(R.id.inputClientId)
        inputClientSecret = view.findViewById(R.id.inputClientSecret)
    }

    fun getClientId(): String = inputClientId.text?.toString()?.trim() ?: ""
    fun getClientSecret(): String = inputClientSecret.text?.toString()?.trim() ?: ""

    fun validate(): Boolean {
        if (getClientId().isEmpty()) {
            Toast.makeText(requireContext(), "Entrez le Client ID", Toast.LENGTH_SHORT).show()
            return false
        }
        if (getClientSecret().isEmpty()) {
            Toast.makeText(requireContext(), "Entrez le Client Secret", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
