package com.votredomaine.webtrees

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class SetupActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var buttonNext: MaterialButton
    private lateinit var step1Indicator: View
    private lateinit var step2Indicator: View
    private lateinit var step3Indicator: View
    
    private lateinit var adapter: SetupPagerAdapter
    private lateinit var prefsManager: PreferencesManager
    
    private var currentStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        
        prefsManager = PreferencesManager(this)
        
        if (prefsManager.isSetupCompleted()) {
            navigateToMainActivity()
            return
        }
        
        initViews()
        setupViewPager()
        setupButtonListeners()
        updateIndicators()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        buttonNext = findViewById(R.id.buttonNext)
        step1Indicator = findViewById(R.id.step1Indicator)
        step2Indicator = findViewById(R.id.step2Indicator)
        step3Indicator = findViewById(R.id.step3Indicator)
    }

    private fun setupViewPager() {
        adapter = SetupPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentStep = position
                updateIndicators()
                updateButtonText()
            }
        })
    }

    private fun setupButtonListeners() {
        buttonNext.setOnClickListener {
            when (currentStep) {
                0 -> handleStep1()
                1 -> handleStep2()
                2 -> handleStep3()
            }
        }
    }

    private fun handleStep1() {
        val fragment = adapter.fragments[0] as SetupUrlFragment
        
        if (fragment.validateUrl()) {
            val url = fragment.getUrl()
            prefsManager.saveSiteUrl(url)
            viewPager.currentItem = 1
        }
    }

    private fun handleStep2() {
        val fragment = adapter.fragments[1] as SetupCredentialsFragment
        
        if (fragment.validateCredentials()) {
            val username = fragment.getUsername()
            val password = fragment.getPassword()
            prefsManager.saveCredentials(username, password)
            viewPager.currentItem = 2
        }
    }

    private fun handleStep3() {
        val fragment = adapter.fragments[2] as SetupTreeFragment
        
        if (fragment.validateTree()) {
            val tree = fragment.getSelectedTree()
            if (tree != null) {
                prefsManager.saveDefaultTree(tree.name)
                prefsManager.setSetupCompleted(true)
                
                Toast.makeText(this, "Configuration terminée !", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            }
        } else {
            Toast.makeText(this, getString(R.string.error_no_tree_selected), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateIndicators() {
        step1Indicator.setBackgroundResource(R.drawable.indicator_inactive)
        step2Indicator.setBackgroundResource(R.drawable.indicator_inactive)
        step3Indicator.setBackgroundResource(R.drawable.indicator_inactive)
        
        when (currentStep) {
            0 -> step1Indicator.setBackgroundResource(R.drawable.indicator_active)
            1 -> step2Indicator.setBackgroundResource(R.drawable.indicator_active)
            2 -> step3Indicator.setBackgroundResource(R.drawable.indicator_active)
        }
    }

    private fun updateButtonText() {
        buttonNext.text = if (currentStep == 2) {
            getString(R.string.button_finish)
        } else {
            getString(R.string.button_next)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (currentStep > 0) {
            viewPager.currentItem = currentStep - 1
        } else {
            super.onBackPressed()
        }
    }
}
