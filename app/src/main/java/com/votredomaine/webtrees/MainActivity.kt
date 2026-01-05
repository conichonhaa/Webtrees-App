package com.votredomaine.webtrees

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.webkit.WebView
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var webViewManager: WebViewManager
    private lateinit var prefsManager: PreferencesManager

    private var baseUrl: String = ""
    private var currentTreeId: String = "sorciers"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefsManager = PreferencesManager(this)

        if (!prefsManager.isSetupCompleted()) {
            navigateToSetup()
            return
        }

        val savedTree = prefsManager.getDefaultTree()
        if (savedTree != null && savedTree.isNotEmpty()) {
            currentTreeId = savedTree
            Log.d("MainActivity", "Loaded saved tree: $currentTreeId")
        }

        initViews()
        setupWebView()
        setupToolbar()
        setupBottomNavigation()
        loadWebtrees()
    }

    private fun initViews() {
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        toolbar = findViewById(R.id.toolbar)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupWebView() {
        val username = prefsManager.getUsername() ?: ""
        val password = prefsManager.getPassword() ?: ""

        webViewManager = WebViewManager(webView, progressBar, username, password)
        webViewManager.setupWebView()

        webViewManager.onPageLoadedListener = {
            runOnUiThread {
                updateToolbarTitle(currentTreeId)
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        updateToolbarTitle(currentTreeId)

        toolbar.setOnClickListener {
            showTreeMenu()
        }

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_change_tree -> {
                    showTreeMenu()
                    true
                }
                R.id.action_settings -> {
                    showSettingsDialog()
                    true
                }
                R.id.action_logout -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    goToHome()
                    true
                }
                R.id.nav_search -> {
                    goToSearch()
                    true
                }
                R.id.nav_stats -> {
                    goToLists()
                    true
                }
                R.id.nav_profile -> {
                    goToReports()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadWebtrees() {
        val siteUrl = prefsManager.getSiteUrl()

        if (siteUrl == null) {
            Toast.makeText(this, "Erreur de configuration", Toast.LENGTH_LONG).show()
            navigateToSetup()
            return
        }

        baseUrl = normalizeUrl(siteUrl)
        goToHome()
    }

    private fun normalizeUrl(url: String): String {
        var normalized = url.trim().trimEnd('/')

        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://$normalized"
        }

        return normalized
    }

    private fun updateToolbarTitle(treeName: String) {
        val displayName = when(treeName) {
            "agp" -> "AGP"
            "disney" -> "Disney"
            "sorciers" -> "Sorciers"
            "Famille" -> "Famille"
            else -> treeName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        toolbar.title = "$displayName ▼"
    }

    private fun showTreeMenu() {
        val trees = arrayOf(
            "sorciers" to "🧙 Sorciers",
            "disney" to "🏰 Disney",
            "agp" to "📜 AGP",
            "Famille" to "👨‍👩‍👧‍👦 Famille",
            "autre" to "➕ Autre arbre..."
        )

        val treeNames = trees.map { it.second }.toTypedArray()
        val treeIds = trees.map { it.first }

        var selectedIndex = treeIds.indexOf(currentTreeId)
        if (selectedIndex == -1) selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Choisir un arbre généalogique")
            .setSingleChoiceItems(treeNames, selectedIndex) { dialog, which ->
                if (treeNames[which].contains("Autre")) {
                    dialog.dismiss()
                    showCustomTreeInput()
                } else {
                    val newTreeId = treeIds[which]
                    Log.d("MainActivity", "Selected tree: '$newTreeId'")

                    currentTreeId = newTreeId
                    updateToolbarTitle(newTreeId)
                    prefsManager.saveDefaultTree(newTreeId)

                    Toast.makeText(this, "Chargement de ${treeNames[which]}", Toast.LENGTH_SHORT).show()
                    goToHome()
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showCustomTreeInput() {
        val input = EditText(this)
        input.hint = "Nom de l'arbre"
        input.setText(currentTreeId)

        val padding = 40
        input.setPadding(padding, padding, padding, padding)

        AlertDialog.Builder(this)
            .setTitle("Nom de l'arbre")
            .setMessage("Entrez le nom EXACT de votre arbre\n(respectez majuscules/minuscules)")
            .setView(input)
            .setPositiveButton("Charger") { _, _ ->
                val treeName = input.text.toString().trim()
                if (treeName.isNotEmpty()) {
                    currentTreeId = treeName
                    updateToolbarTitle(treeName)
                    prefsManager.saveDefaultTree(treeName)
                    goToHome()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun goToHome() {
        // FORMAT EXACT: route=%2Ftree%2F[arbre]
        val homeUrl = "$baseUrl/index.php?route=%2Ftree%2F$currentTreeId"
        Log.d("MainActivity", "Loading URL: $homeUrl")
        webViewManager.loadUrl(homeUrl)
    }

    private fun goToSearch() {
        // Format: route=%2Ftree%2F[arbre]%2Fsearch-general
        val searchUrl = "$baseUrl/index.php?route=%2Ftree%2F${currentTreeId}%2Fsearch-general"
        Log.d("MainActivity", "Search URL: $searchUrl")
        webViewManager.loadUrl(searchUrl)
    }

    private fun goToLists() {
        // Format: route=%2Ftree%2F[arbre]%2Findividual-list
        val listUrl = "$baseUrl/index.php?route=%2Ftree%2F${currentTreeId}%2Findividual-list"
        Log.d("MainActivity", "List URL: $listUrl")
        webViewManager.loadUrl(listUrl)
    }

    private fun goToReports() {
        // Ma page personnelle
        val myPageUrl = "$baseUrl/index.php?route=%2Ftree%2F${currentTreeId}%2Fmy-page"
        Log.d("MainActivity", "My page URL: $myPageUrl")
        webViewManager.loadUrl(myPageUrl)
    }

    private fun showSettingsDialog() {
        val options = arrayOf(
            "🔄 Recharger la page",
            "🗑️ Effacer le cache",
            "🐛 Voir l'URL actuelle",
            "⚙️ Reconfigurer l'application",
            "ℹ️ À propos"
        )

        AlertDialog.Builder(this)
            .setTitle("Paramètres")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        webView.reload()
                        Toast.makeText(this, "Rechargement...", Toast.LENGTH_SHORT).show()
                    }
                    1 -> clearCache()
                    2 -> {
                        val debugUrl = "$baseUrl/index.php?route=%2Ftree%2F$currentTreeId"
                        AlertDialog.Builder(this)
                            .setTitle("Debug URL")
                            .setMessage("Arbre actuel : '$currentTreeId'\n\nURL :\n$debugUrl")
                            .setPositiveButton("Copier l'arbre") { _, _ ->
                                Toast.makeText(this, "Arbre: $currentTreeId", Toast.LENGTH_LONG).show()
                            }
                            .setNegativeButton("OK", null)
                            .show()
                    }
                    3 -> reconfigureApp()
                    4 -> showAboutDialog()
                }
            }
            .show()
    }

    private fun clearCache() {
        webViewManager.clearCache()
        Toast.makeText(this, "Cache effacé", Toast.LENGTH_SHORT).show()
        goToHome()
    }

    private fun reconfigureApp() {
        AlertDialog.Builder(this)
            .setTitle("Reconfigurer l'application")
            .setMessage("Vous allez devoir ressaisir vos identifiants.\n\nContinuer ?")
            .setPositiveButton("Oui") { _, _ ->
                prefsManager.clearAll()
                navigateToSetup()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun showAboutDialog() {
        val info = """
            📱 Application Webtrees Mobile
            Version 1.0
            
            🌳 Arbre actuel : ${getTreeDisplayName(currentTreeId)}
            🌐 Site : ${baseUrl.replace("https://", "")}
            
            Vos arbres généalogiques :
            • 🧙 Sorciers
            • 🏰 Disney
            • 📜 AGP
            • 👨‍👩‍👧‍👦 Famille
            
            Navigation :
            • Cliquez sur le titre pour changer d'arbre
            • 🏠 Accueil : Page principale
            • 🔍 Recherche : Chercher des personnes
            • 📋 Listes : Liste des individus
            • 👤 Profil : Ma page personnelle
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("À propos")
            .setMessage(info)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getTreeDisplayName(treeId: String): String {
        return when(treeId) {
            "sorciers" -> "🧙 Sorciers"
            "disney" -> "🏰 Disney"
            "agp" -> "📜 AGP"
            "Famille" -> "👨‍👩‍👧‍👦 Famille"
            else -> treeId
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Se déconnecter de l'application ?")
            .setPositiveButton("Oui") { _, _ ->
                prefsManager.clearAll()
                navigateToSetup()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun navigateToSetup() {
        val intent = Intent(this, SetupActivity::class.java)
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webViewManager.canGoBack()) {
            webViewManager.goBack()
        } else {
            super.onBackPressed()
        }
    }
}