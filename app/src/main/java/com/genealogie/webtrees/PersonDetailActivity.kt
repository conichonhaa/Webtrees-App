package com.genealogie.webtrees

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
    private lateinit var containerMarriages: LinearLayout
    private lateinit var containerParents: LinearLayout
    private lateinit var containerChildren: LinearLayout
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_detail)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        progressBar         = findViewById(R.id.progressBar)
        textName            = findViewById(R.id.textName)
        textGender          = findViewById(R.id.textGender)
        textBirth           = findViewById(R.id.textBirth)
        textDeath           = findViewById(R.id.textDeath)
        textXref            = findViewById(R.id.textXref)
        containerMarriages  = findViewById(R.id.containerMarriages)
        containerParents    = findViewById(R.id.containerParents)
        containerChildren   = findViewById(R.id.containerChildren)

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

        if (person.marriages.isNotEmpty()) {
            containerMarriages.visibility = View.VISIBLE
            for (marriage in person.marriages) {
                containerMarriages.addView(buildSectionCard(
                    title = "Mariage",
                    lines = buildList {
                        add("Conjoint : ${marriage.spouseName.ifEmpty { marriage.spouseXref }}")
                        if (marriage.date.isNotEmpty())  add("Date : ${marriage.date}")
                        if (marriage.place.isNotEmpty()) add("Lieu : ${marriage.place}")
                    }
                ))
            }
        }

        if (person.parents.isNotEmpty()) {
            containerParents.visibility = View.VISIBLE
            containerParents.addView(buildSectionCard(
                title = "Parents",
                lines = person.parents.map { it.name.ifEmpty { it.xref } }
            ))
        }

        if (person.children.isNotEmpty()) {
            containerChildren.visibility = View.VISIBLE
            containerChildren.addView(buildSectionCard(
                title = "Enfants (${person.children.size})",
                lines = person.children.map { it.name.ifEmpty { it.xref } }
            ))
        }
    }

    private fun buildSectionCard(title: String, lines: List<String>): CardView {
        val dp8  = (8  * resources.displayMetrics.density).toInt()
        val dp12 = (12 * resources.displayMetrics.density).toInt()
        val dp16 = (16 * resources.displayMetrics.density).toInt()

        val innerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp16, dp16, dp16, dp16)
        }

        innerLayout.addView(TextView(this).apply {
            text = title.uppercase()
            textSize = 12f
            setTextColor(android.graphics.Color.GRAY)
            setPadding(0, 0, 0, dp8)
        })

        for (line in lines) {
            innerLayout.addView(TextView(this).apply {
                text = line
                textSize = 16f
                setTextColor(android.graphics.Color.BLACK)
                setPadding(0, 0, 0, dp8 / 2)
            })
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, dp12) }

        return CardView(this).apply {
            radius = dp8.toFloat()
            cardElevation = dp8 * 0.375f
            layoutParams = params
            addView(innerLayout)
        }
    }
}
