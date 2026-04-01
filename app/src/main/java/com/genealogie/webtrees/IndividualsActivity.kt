package com.genealogie.webtrees

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class IndividualsActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var searchInput: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var prefsManager: PreferencesManager
    private lateinit var api: WebtreesApi

    private var trees: List<Tree> = emptyList()
    private var selectedTree: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individuals)

        prefsManager = PreferencesManager(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        spinner      = findViewById(R.id.spinnerTree)
        searchInput  = findViewById(R.id.searchInput)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar  = findViewById(R.id.progressBar)
        emptyText    = findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val siteUrl = prefsManager.getSiteUrl() ?: return
        val clientId = prefsManager.getOAuthClientId() ?: return
        val clientSecret = prefsManager.getOAuthClientSecret() ?: return
        api = WebtreesApi(siteUrl, clientId, clientSecret)

        loadTrees()

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search()
                true
            } else false
        }
    }

    private fun loadTrees() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                trees = api.getTrees()
                val titles = trees.map { it.title }
                spinner.adapter = ArrayAdapter(
                    this@IndividualsActivity,
                    android.R.layout.simple_spinner_item,
                    titles
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                val savedTree = prefsManager.getDefaultTree()
                val idx = trees.indexOfFirst { it.name == savedTree }
                if (idx >= 0) spinner.setSelection(idx)

                selectedTree = trees.getOrNull(spinner.selectedItemPosition)?.name ?: ""

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                        selectedTree = trees[pos].name
                        recyclerView.adapter = null
                        emptyText.text = "Tapez un nom pour rechercher"
                        emptyText.visibility = View.VISIBLE
                    }
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            } catch (e: Exception) {
                showError("Erreur chargement des arbres : ${e.message}")
            } finally {
                showLoading(false)
                emptyText.text = "Tapez un nom pour rechercher"
                emptyText.visibility = View.VISIBLE
            }
        }
    }

    private fun search() {
        val query = searchInput.text?.toString()?.trim() ?: ""
        if (query.isEmpty()) {
            Toast.makeText(this, "Entrez un nom à rechercher", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedTree.isEmpty()) {
            Toast.makeText(this, "Sélectionnez un arbre", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        emptyText.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val results = api.searchPersons(selectedTree, query)
                if (results.isEmpty()) {
                    emptyText.text = "Aucun résultat pour \"$query\""
                    emptyText.visibility = View.VISIBLE
                    recyclerView.adapter = null
                } else {
                    emptyText.visibility = View.GONE
                    recyclerView.adapter = PersonAdapter(results) { person ->
                        val intent = Intent(this@IndividualsActivity, PersonDetailActivity::class.java)
                        intent.putExtra("tree", person.tree)
                        intent.putExtra("xref", person.xref)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                showError("Erreur recherche : ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(msg: String) {
        emptyText.text = msg
        emptyText.visibility = View.VISIBLE
        recyclerView.adapter = null
    }
}
