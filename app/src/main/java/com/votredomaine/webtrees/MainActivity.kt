package com.votredomaine.webtrees

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.webkit.WebView

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNavigation: BottomNavigationView
    
    private lateinit var webViewManager: WebViewManager
    private lateinit var prefsManager: PreferencesManager
    
    private var currentTreeName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefsManager = PreferencesManager(this)
        
        if (!prefsManager.isSetupCompleted()) {
            navigateToSetup()
            return
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
        webViewManager = WebViewManager(webView, progressBar)
        webViewManager.setupWebView()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_change_tree -> {
                    showTreeSelectionDialog()
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
            val siteUrl = prefsManager.getSiteUrl() ?: return@setOnItemSelectedListener false
            val tree = currentTreeName ?: prefsManager.getDefaultTree() ?: ""
            
            when (item.itemId) {
                R.id.nav_home -> {
                    webViewManager.loadUrl("$siteUrl/index.php?route=%2F$tree%2Ftree")
                    true
                }
                R.id.nav_search -> {
                    webViewManager.loadUrl("$siteUrl/index.php?route=%2F$tree%2Fsearch-general")
                    true
                }
                R.id.nav_stats -> {
                    webViewManager.loadUrl("$siteUrl/index.php?route=%2F$tree%2Freport-list")
                    true
                }
                R.id.nav_profile -> {
                    webViewManager.loadUrl("$siteUrl/index.php?route=%2F$tree%2Findividual-list")
                    true
                }
                else -> false
            }
        }
    }

    private fun loadWebtrees() {
        val siteUrl = prefsManager.getSiteUrl()
        currentTreeName = prefsManager.getDefaultTree()
        
        if (siteUrl == null || currentTreeName == null) {
            Toast.makeText(this, "Erreur de configuration", Toast.LENGTH_LONG).show()
            navigateToSetup()
            return
        }
        
        val url = "$siteUrl/index.php?route=%2F$currentTreeName%2Ftree"
        webViewManager.loadUrl(url)
        
        updateToolbarTitle()
    }

    private fun updateToolbarTitle() {
        currentTreeName?.let { treeName ->
            toolbar.title = treeName.replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.capitalize() }
        }
    }

    private fun showTreeSelectionDialog() {
        val trees = arrayOf(
            "Famille Dupont",
            "Famille Martin",
            "Arbre Principal"
        )
        
        val treeNames = arrayOf("tree1", "tree2", "tree3")
        
        var selectedIndex = treeNames.indexOf(currentTreeName)
        if (selectedIndex == -1) selectedIndex = 0
        
        AlertDialog.Builder(this)
            .setTitle("Choisir un arbre")
            .setSingleChoiceItems(trees, selectedIndex) { dialog, which ->
                currentTreeName = treeNames[which]
                prefsManager.saveDefaultTree(currentTreeName!!)
                updateToolbarTitle()
                loadWebtrees()
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showSettingsDialog() {
        val options = arrayOf(
            "Effacer le cache",
            "Reconfigurer l'application",
            "À propos"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Paramètres")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> clearCache()
                    1 -> reconfigureApp()
                    2 -> showAboutDialog()
                }
            }
            .show()
    }

    private fun clearCache() {
        webViewManager.clearCache()
        Toast.makeText(this, "Cache effacé", Toast.LENGTH_SHORT).show()
        loadWebtrees()
    }

    private fun reconfigureApp() {
        AlertDialog.Builder(this)
            .setTitle("Reconfigurer")
            .setMessage("Voulez-vous vraiment reconfigurer l'application ? Vous devrez saisir à nouveau vos informations de connexion.")
            .setPositiveButton("Oui") { _, _ ->
                prefsManager.clearAll()
                navigateToSetup()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("À propos")
            .setMessage("Application Webtrees Mobile\nVersion 1.0\n\nCette application vous permet d'accéder à votre arbre généalogique Webtrees de manière optimisée pour mobile.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Voulez-vous vous déconnecter ?")
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

    override fun onBackPressed() {
        if (webViewManager.canGoBack()) {
            webViewManager.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }
}
