package com.genealogie.webtrees

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.webkit.WebView
import android.util.Log
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var webViewManager: WebViewManager
    private lateinit var prefsManager: PreferencesManager

    private var baseUrl: String = ""
    private var currentTreeId: String = "sorciers"
    private var cachedTrees: List<Tree> = emptyList()

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

        // Load cached trees immediately (sync, no network)
        cachedTrees = prefsManager.getCachedTrees()

        initViews()
        setupWebView()
        setupToolbar()
        setupBottomNavigation()
        setupBackPressHandler()
        loadWebtrees()

        // Refresh tree list from API in background on every start
        refreshTreesFromApi()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webViewManager.canGoBack()) {
                    webViewManager.goBack()
                } else {
                    finish()
                }
            }
        })
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
                R.id.action_optimize -> {
                    optimizeDisplay()
                    true
                }
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

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu_final, menu)
        return true
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    goToHome()
                    true
                }
                R.id.nav_search -> {
                    openIndividuals()
                    true
                }
                R.id.nav_stats -> {
                    openIndividuals()
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

    private fun refreshTreesFromApi() {
        if (!prefsManager.hasOAuthCredentials()) return
        val siteUrl      = prefsManager.getSiteUrl() ?: return
        val clientId     = prefsManager.getOAuthClientId() ?: return
        val clientSecret = prefsManager.getOAuthClientSecret() ?: return
        val api = WebtreesApi(siteUrl, clientId, clientSecret)
        lifecycleScope.launch {
            try {
                val trees = api.getTrees()
                if (trees.isNotEmpty()) {
                    cachedTrees = trees
                    prefsManager.saveCachedTrees(trees)
                    Log.d("MainActivity", "Trees refreshed from API: ${trees.map { it.name }}")
                }
            } catch (e: Exception) {
                Log.w("MainActivity", "Could not refresh trees: ${e.message}")
            }
        }
    }

    private fun updateToolbarTitle(treeId: String) {
        val displayName = cachedTrees.find { it.name == treeId }?.title
            ?: treeId.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        toolbar.title = "$displayName ▼"
    }

    private fun showTreeMenu() {
        // Build list from API cache, fallback to empty if not yet loaded
        val apiTrees = cachedTrees.map { it.name to it.title }
        val allEntries = (if (apiTrees.isNotEmpty()) apiTrees else listOf(
            "sorciers" to "Sorciers",
            "disney" to "Disney",
            "agp" to "AGP",
            "Famille" to "Famille"
        )) + listOf("__custom__" to "➕ Autre arbre...")

        val treeNames = allEntries.map { it.second }.toTypedArray()
        val treeIds   = allEntries.map { it.first }

        var selectedIndex = treeIds.indexOf(currentTreeId)
        if (selectedIndex == -1) selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Choisir un arbre généalogique")
            .setSingleChoiceItems(treeNames, selectedIndex) { dialog, which ->
                if (treeIds[which] == "__custom__") {
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

    private fun openIndividuals() {
        if (!prefsManager.hasOAuthCredentials()) {
            showOAuthCredentialsDialog()
            return
        }
        startActivity(Intent(this, IndividualsActivity::class.java))
    }

    private fun showOAuthCredentialsDialog() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        val inputId = EditText(this).apply { hint = "Client ID"; setText(prefsManager.getOAuthClientId() ?: "") }
        val inputSecret = EditText(this).apply { hint = "Client Secret"; setText(prefsManager.getOAuthClientSecret() ?: "") }
        layout.addView(android.widget.TextView(this).apply { text = "Identifiants OAuth2 webtrees-API" ; setPadding(0,0,0,16) })
        layout.addView(inputId)
        layout.addView(inputSecret)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Configuration API")
            .setView(layout)
            .setPositiveButton("Enregistrer") { _, _ ->
                val id = inputId.text.toString().trim()
                val secret = inputSecret.text.toString().trim()
                if (id.isNotEmpty() && secret.isNotEmpty()) {
                    prefsManager.saveOAuthCredentials(id, secret)
                    startActivity(Intent(this, IndividualsActivity::class.java))
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
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
            "✨ Optimiser l'affichage",
            "🗑️ Effacer le cache",
            "🌐 Modifier l'URL du site",
            "🔑 Identifiants API (OAuth2)",
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
                    1 -> optimizeDisplay()
                    2 -> clearCache()
                    3 -> showEditUrlDialog()
                    4 -> showOAuthCredentialsDialog()
                    5 -> {
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
                    6 -> reconfigureApp()
                    7 -> showAboutDialog()
                }
            }
            .show()
    }

    private fun optimizeDisplay() {
        val js = """
            javascript:(function() {
                var rows = document.querySelectorAll('table tbody tr');
                var count = 0;
                rows.forEach(function(tr) {
                    var th = tr.querySelector('th');
                    var td = tr.querySelector('td');
                    if (th && td) {
                        tr.setAttribute('style', 'display: block !important; padding: 16px 12px !important; border-bottom: 1px solid #e0e0e0 !important;');
                        th.setAttribute('style', 'display: block !important; width: 100% !important; font-weight: 700 !important; font-size: 16px !important; color: #667eea !important; margin-bottom: 10px !important;');
                        td.setAttribute('style', 'display: block !important; width: 100% !important; font-weight: 400 !important; color: #333 !important;');
                        count++;
                    }
                });
                if (count > 0) {
                    try { window.AndroidInterface.showToast('Affichage optimisé !'); } catch(e) {}
                } else {
                    try { window.AndroidInterface.showToast('Aucune statistique trouvée'); } catch(e) {}
                }
            })()
        """
        webViewManager.executeJavaScript(js)
    }

    private fun clearCache() {
        webViewManager.clearCache()
        Toast.makeText(this, "Cache effacé", Toast.LENGTH_SHORT).show()
        goToHome()
    }

    private fun showEditUrlDialog() {
        val currentUrl = prefsManager.getSiteUrl() ?: ""

        val input = EditText(this).apply {
            setText(currentUrl)
            hint = "https://exemple.com/webtrees"
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Modifier l'URL du site")
            .setMessage("URL actuelle :\n$currentUrl\n\nEntrez la nouvelle URL :")
            .setView(input)
            .setPositiveButton("Enregistrer") { _, _ ->
                val newUrl = input.text.toString().trim()

                if (newUrl.isEmpty()) {
                    Toast.makeText(this, "L'URL ne peut pas être vide", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                    Toast.makeText(this, "L'URL doit commencer par http:// ou https://", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                // Sauvegarder la nouvelle URL
                prefsManager.saveSiteUrl(newUrl)
                baseUrl = normalizeUrl(newUrl)

                // Afficher confirmation
                Toast.makeText(this, "URL mise à jour !", Toast.LENGTH_SHORT).show()

                // Recharger la page d'accueil
                webViewManager.clearCache()
                goToHome()
            }
            .setNegativeButton("Annuler", null)
            .setNeutralButton("Tester") { _, _ ->
                val testUrl = input.text.toString().trim()
                if (testUrl.isNotEmpty()) {
                    webViewManager.loadUrl(testUrl)
                    Toast.makeText(this, "Test de l'URL...", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
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
        return cachedTrees.find { it.name == treeId }?.title ?: treeId
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
}