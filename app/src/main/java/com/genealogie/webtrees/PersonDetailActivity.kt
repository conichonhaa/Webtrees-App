package com.genealogie.webtrees

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class PersonDetailActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var textName: TextView
    private lateinit var textGender: TextView
    private lateinit var textBirth: TextView
    private lateinit var textDeath: TextView
    private lateinit var textXref: TextView
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        progressBar = findViewById(R.id.progressBar)
        textName    = findViewById(R.id.textName)
        textGender  = findViewById(R.id.textGender)
        textBirth   = findViewById(R.id.textBirth)
        textDeath   = findViewById(R.id.textDeath)
        textXref    = findViewById(R.id.textXref)

        val tree = intent.getStringExtra("tree") ?: return
        val xref = intent.getStringExtra("xref") ?: return

        prefsManager = PreferencesManager(this)
        val siteUrl      = prefsManager.getSiteUrl() ?: return
        val clientId     = prefsManager.getOAuthClientId() ?: return
        val clientSecret = prefsManager.getOAuthClientSecret() ?: return
        val api = WebtreesApi(siteUrl, clientId, clientSecret)

        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val person = api.getPersonDetail(tree, xref)
                displayPerson(person)
            } catch (e: Exception) {
                textName.text = "Erreur : ${e.message}"
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayPerson(person: Person) {
        supportActionBar?.title = person.fullName.ifEmpty { person.xref }
        textName.text   = person.fullName.ifEmpty { "Nom inconnu" }
        textGender.text = person.gender.ifEmpty { "Non renseigné" }
        textXref.text   = "${person.xref} — ${person.tree}"

        textBirth.text = buildString {
            if (person.birthDate.isNotEmpty())  append(person.birthDate)
            if (person.birthPlace.isNotEmpty()) append(" à ${person.birthPlace}")
        }.ifEmpty { "Non renseigné" }

        textDeath.text = buildString {
            if (person.deathDate.isNotEmpty())  append(person.deathDate)
            if (person.deathPlace.isNotEmpty()) append(" à ${person.deathPlace}")
        }.ifEmpty { "Non renseigné" }
    }
}
