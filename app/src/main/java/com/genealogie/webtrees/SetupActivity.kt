package com.genealogie.webtrees

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentStep > 0) {
                    viewPager.currentItem = currentStep - 1
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
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
            prefsManager.saveSiteUrl(fragment.getUrl())
            viewPager.currentItem = 1
        }
    }

    private fun handleStep2() {
        val fragment = adapter.fragments[1] as SetupCredentialsFragment
        if (fragment.validateCredentials()) {
            prefsManager.saveCredentials(fragment.getUsername(), fragment.getPassword())
            viewPager.currentItem = 2
        }
    }

    private fun handleStep3() {
        val fragment = adapter.fragments[2] as SetupApiFragment
        if (fragment.validate()) {
            prefsManager.saveOAuthCredentials(fragment.getClientId(), fragment.getClientSecret())
            prefsManager.saveDefaultTree("default")
            prefsManager.setSetupCompleted(true)
            Toast.makeText(this, "Configuration terminée !", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
        }
    }

    private fun updateIndicators() {
        val inactive = R.drawable.indicator_inactive
        val active   = R.drawable.indicator_active
        step1Indicator.setBackgroundResource(inactive)
        step2Indicator.setBackgroundResource(inactive)
        step3Indicator.setBackgroundResource(inactive)
        when (currentStep) {
            0 -> step1Indicator.setBackgroundResource(active)
            1 -> step2Indicator.setBackgroundResource(active)
            2 -> step3Indicator.setBackgroundResource(active)
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
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
