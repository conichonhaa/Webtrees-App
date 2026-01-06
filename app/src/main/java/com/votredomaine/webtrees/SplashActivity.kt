package com.votredomaine.webtrees

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2000L // 2 secondes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Démarrer l'activité principale après le délai
        Handler(Looper.getMainLooper()).postDelayed({
            startMainActivity()
        }, SPLASH_DELAY)
    }

    private fun startMainActivity() {
        val prefsManager = PreferencesManager(this)

        val intent = if (prefsManager.isSetupCompleted()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, SetupActivity::class.java)
        }

        startActivity(intent)
        finish()

        // Animation de transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}