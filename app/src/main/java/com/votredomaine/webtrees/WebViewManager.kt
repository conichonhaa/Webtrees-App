package com.votredomaine.webtrees

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.net.http.SslError
import android.view.View
import android.widget.ProgressBar
import android.util.Log

class WebViewManager(
    private val webView: WebView,
    private val progressBar: ProgressBar,
    private val username: String,
    private val password: String
) {

    private var hasAttemptedLogin = false
    var onPageLoadedListener: (() -> Unit)? = null

    fun setupWebView() {
        // Ajouter l'interface JavaScript pour les toasts
        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun showToast(message: String) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        webView.context,
                        message,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }, "AndroidInterface")

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.e("WebViewManager", "Error: ${error?.description}")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("WebViewManager", "Page finished: $url")

                if (url?.contains("login") == true && !hasAttemptedLogin) {
                    hasAttemptedLogin = true
                    view?.postDelayed({
                        attemptAutoLogin()
                    }, 500)
                } else {
                    view?.postDelayed({
                        injectAggressiveCSS()
                        onPageLoadedListener?.invoke()
                    }, 2000)  // Augmenté à 2 secondes
                }

                hideProgressBar()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress < 100) {
                    showProgressBar()
                    progressBar.progress = newProgress
                } else {
                    hideProgressBar()
                }
            }
        }
    }

    private fun attemptAutoLogin() {
        val jsCode = """
            javascript:(function() {
                try {
                    var usernameField = document.querySelector('input[name="username"]') || 
                                       document.querySelector('input[type="text"]');
                    var passwordField = document.querySelector('input[name="password"]') || 
                                       document.querySelector('input[type="password"]');
                    var loginForm = document.querySelector('form');
                    
                    if (usernameField && passwordField) {
                        usernameField.value = '$username';
                        passwordField.value = '$password';
                        if (loginForm) {
                            loginForm.submit();
                        }
                    }
                } catch(e) {}
            })()
        """.trimIndent()

        webView.loadUrl(jsCode)
    }

    private fun injectAggressiveCSS() {
        val css = """
            javascript:(function() {
                // Supprimer les anciennes feuilles de style
                var oldStyle = document.getElementById('mobile-override');
                if (oldStyle) oldStyle.remove();
                
                var style = document.createElement('style');
                style.id = 'mobile-override';
                style.innerHTML = `
                    /* FORCER LE STYLE - IMPORTANT PARTOUT */
                    * {
                        box-sizing: border-box !important;
                    }
                    
                    body, html {
                        margin: 0 !important;
                        padding: 0 !important;
                        background: #f0f0f0 !important;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
                        font-size: 15px !important;
                        line-height: 1.5 !important;
                        color: #333 !important;
                        overflow-x: hidden !important;
                    }
                    
                    /* CACHER TOUT LE CHROME WEBTREES */
                    header, nav, footer,
                    .wt-header-wrapper, .wt-header-container,
                    .wt-footer-wrapper, .wt-footer-container,
                    .wt-primary-navigation, .wt-secondary-navigation,
                    .wt-user-menu, .breadcrumb,
                    [class*="header"], [class*="Header"],
                    [class*="footer"], [class*="Footer"],
                    [class*="navigation"], [class*="Navigation"],
                    [id*="header"], [id*="Header"],
                    [id*="footer"], [id*="Footer"] {
                        display: none !important;
                        visibility: hidden !important;
                        height: 0 !important;
                        opacity: 0 !important;
                    }
                    
                    /* SAUF LE SÉLECTEUR D'ARBRE QU'ON CACHE AUSSI CAR ON LE GÈRE DANS L'APP */
                    .wt-tree-name, .tree-menu, select[name="tree"],
                    .control-panel, .wt-tree-selector {
                        display: none !important;
                    }
                    
                    /* FORCER CONTAINER PLEINE LARGEUR */
                    .container, .container-fluid,
                    .wt-main-wrapper, .wt-main-container,
                    .wt-main, main, #content,
                    .wt-page-content, .row {
                        width: 100% !important;
                        max-width: 100% !important;
                        margin: 0 !important;
                        padding: 0 !important;
                    }
                    
                    /* SIDEBAR */
                    .wt-sidebar, aside, .col-sm-3, .col-md-3, .col-lg-3 {
                        display: none !important;
                    }
                    
                    /* CONTENU PRINCIPAL PLEINE LARGEUR */
                    .col-sm-9, .col-md-9, .col-lg-9,
                    .wt-main-container > div {
                        width: 100% !important;
                        flex: 0 0 100% !important;
                        max-width: 100% !important;
                        padding: 0 !important;
                    }
                    
                    /* CARTES BLANCHES STYLE APP */
                    .card, .wt-facts-table,
                    .person-box, .wt-individual-list-item,
                    .wt-page-options, section, article,
                    [class*="card"], [class*="Card"],
                    table.table {
                        background: white !important;
                        border: none !important;
                        border-radius: 0 !important;
                        margin: 0 0 2px 0 !important;
                        padding: 16px !important;
                        box-shadow: none !important;
                    }
                    
                    /* TITRES */
                    h1, h2, h3, h4, h5, h6 {
                        font-weight: 600 !important;
                        margin: 12px 0 8px 0 !important;
                        padding: 0 !important;
                    }
                    
                    h1 { 
                        font-size: 22px !important; 
                        color: #1a1a1a !important;
                    }
                    h2 { 
                        font-size: 18px !important; 
                        color: #667eea !important;
                    }
                    h3 { 
                        font-size: 16px !important; 
                        color: #333 !important;
                    }
                    
                    /* TABLES EN MODE LISTE */
                    table {
                        width: 100% !important;
                        border-collapse: collapse !important;
                        border: none !important;
                        background: transparent !important;
                    }
                    
                    table thead {
                        background: #f5f5f5 !important;
                    }
                    
                    table th {
                        padding: 10px 8px !important;
                        font-size: 13px !important;
                        font-weight: 600 !important;
                        color: #666 !important;
                        border-bottom: 2px solid #e0e0e0 !important;
                    }
                    
                    table td {
                        padding: 10px 8px !important;
                        font-size: 14px !important;
                        border-bottom: 1px solid #f0f0f0 !important;
                    }
                    
                    table tr:hover {
                        background: #f9f9f9 !important;
                    }
                    
                    /* FORCER LES STATISTIQUES EN MODE VERTICAL */
                    /* Cibler les tables de statistiques (après chargement AJAX) */
                    .wt-facts-table tbody tr,
                    table.table-sm tbody tr,
                    table.wt-facts-table tbody tr {
                        display: block !important;
                        padding: 16px 12px !important;
                        border-bottom: 1px solid #e0e0e0 !important;
                        margin-bottom: 0 !important;
                    }
                    
                    .wt-facts-table tbody td,
                    table.table-sm tbody td,
                    table.wt-facts-table tbody td {
                        display: block !important;
                        width: 100% !important;
                        text-align: left !important;
                        padding: 3px 0 !important;
                        border: none !important;
                        float: none !important;
                    }
                    
                    /* Premier TD = Titre (Première naissance, Dernier décès, etc.) */
                    .wt-facts-table tbody td:first-child,
                    table.table-sm tbody td:first-child,
                    table.wt-facts-table tbody td:first-child {
                        font-weight: 700 !important;
                        font-size: 15px !important;
                        color: #667eea !important;
                        margin-bottom: 10px !important;
                        display: block !important;
                    }
                    
                    /* Deuxième TD = Contenu (nom + détails) */
                    .wt-facts-table tbody td:last-child,
                    table.table-sm tbody td:last-child,
                    table.wt-facts-table tbody td:last-child {
                        font-weight: 400 !important;
                        color: #333 !important;
                        line-height: 1.7 !important;
                        display: block !important;
                    }
                    
                    /* Forcer les liens et éléments inline à s'afficher en block */
                    .wt-facts-table tbody td:last-child a,
                    .wt-facts-table tbody td:last-child br,
                    table.table-sm tbody td:last-child a,
                    table.table-sm tbody td:last-child br {
                        display: inline !important;
                    }
                    
                    /* LIENS */
                    a {
                        color: #667eea !important;
                        text-decoration: none !important;
                        font-weight: 500 !important;
                    }
                    
                    a:active {
                        opacity: 0.7 !important;
                    }
                    
                    /* BOUTONS */
                    .btn, button, 
                    input[type="submit"], input[type="button"],
                    [class*="btn"], [class*="Btn"],
                    [class*="button"], [class*="Button"] {
                        background: linear-gradient(135deg, #667eea, #764ba2) !important;
                        color: white !important;
                        border: none !important;
                        padding: 10px 20px !important;
                        border-radius: 8px !important;
                        font-size: 14px !important;
                        font-weight: 600 !important;
                        min-height: 44px !important;
                        display: inline-block !important;
                        text-align: center !important;
                    }
                    
                    .btn-secondary, .btn-outline {
                        background: white !important;
                        color: #667eea !important;
                        border: 2px solid #667eea !important;
                    }
                    
                    /* FORMULAIRES */
                    input[type="text"], input[type="search"],
                    input[type="email"], input[type="password"],
                    input[type="number"], input[type="tel"],
                    input[type="date"], select, textarea {
                        width: 100% !important;
                        padding: 12px !important;
                        border: 1px solid #ddd !important;
                        border-radius: 8px !important;
                        font-size: 15px !important;
                        margin: 6px 0 !important;
                        background: white !important;
                    }
                    
                    /* IMAGES */
                    img {
                        max-width: 100% !important;
                        height: auto !important;
                    }
                    
                    /* IMAGES DE PROFIL */
                    img[alt*="photo"], img[alt*="Photo"],
                    .wt-individual-silhouette {
                        width: 100px !important;
                        height: 100px !important;
                        border-radius: 50% !important;
                        object-fit: cover !important;
                        display: block !important;
                        margin: 0 auto 16px auto !important;
                    }
                    
                    /* LISTES D'INDIVIDUS */
                    .wt-individual-list-item,
                    .person-box {
                        border-left: 4px solid #667eea !important;
                        margin-bottom: 2px !important;
                    }
                    
                    /* BADGES */
                    .badge, .label, .tag {
                        background: #667eea !important;
                        color: white !important;
                        padding: 4px 10px !important;
                        border-radius: 12px !important;
                        font-size: 12px !important;
                        font-weight: 600 !important;
                        display: inline-block !important;
                    }
                    
                    /* ESPACEMENTS */
                    p { margin: 8px 0 !important; }
                    ul, ol { padding-left: 20px !important; }
                    li { margin: 6px 0 !important; }
                    
                    /* GRAPHIQUES ET CHARTS */
                    .wt-chart-box, svg {
                        max-width: 100% !important;
                        height: auto !important;
                    }
                    
                    /* BOUTONS DE FILTRE (sur la page d'accueil) */
                    .btn-group, .btn-toolbar {
                        display: flex !important;
                        flex-wrap: wrap !important;
                        gap: 8px !important;
                        margin: 12px 0 !important;
                    }
                    
                    .btn-group > * {
                        flex: 1 1 auto !important;
                        min-width: 80px !important;
                    }
                `;
                
                document.head.appendChild(style);
                
                // Fonction pour forcer les styles sur les statistiques
                function forceStatsStyles() {
                    var found = false;
                    
                    // Chercher les lignes de statistiques (elles utilisent TH au lieu de TD!)
                    var rows = document.querySelectorAll('table tbody tr');
                    
                    if (rows.length > 0) {
                        found = true;
                        console.log('Found ' + rows.length + ' stat rows');
                        
                        rows.forEach(function(tr) {
                            // Vérifier si la ligne contient un TH (titre de statistique)
                            var th = tr.querySelector('th');
                            var td = tr.querySelector('td');
                            
                            if (th && td) {
                                // Forcer l'affichage en block avec !important
                                tr.setAttribute('style', 'display: block !important; padding: 16px 12px !important; border-bottom: 1px solid #e0e0e0 !important; width: 100% !important;');
                                
                                // TH = Titre avec !important sur TOUT
                                th.setAttribute('style', 'display: block !important; width: 100% !important; text-align: left !important; padding: 3px 0 !important; border: none !important; font-weight: 700 !important; font-size: 16px !important; color: #667eea !important; margin-bottom: 10px !important;');
                                
                                // TD = Contenu avec !important sur TOUT
                                td.setAttribute('style', 'display: block !important; width: 100% !important; text-align: left !important; padding: 3px 0 !important; border: none !important; font-weight: 400 !important; color: #333 !important; line-height: 1.7 !important;');
                            }
                        });
                    }
                    
                    if (found) {
                        console.log('Stats tables styled successfully!');
                        try {
                            window.AndroidInterface.showToast('Stats OK!');
                        } catch(e) {}
                    }
                    
                    return found;
                }
                
                // Appliquer immédiatement puis répéter plusieurs fois
                setTimeout(forceStatsStyles, 500);
                setTimeout(forceStatsStyles, 1000);
                setTimeout(forceStatsStyles, 1500);
                setTimeout(forceStatsStyles, 2000);
                setTimeout(forceStatsStyles, 2500);
                setTimeout(forceStatsStyles, 3000);
                setTimeout(forceStatsStyles, 4000);
                setTimeout(forceStatsStyles, 5000);
                setTimeout(forceStatsStyles, 6000);
                setTimeout(forceStatsStyles, 7000);
                setTimeout(forceStatsStyles, 8000);
                setTimeout(forceStatsStyles, 10000);
                
                // Et continuer à appliquer toutes les 5 secondes indéfiniment
                setInterval(forceStatsStyles, 5000);
                
                // Forcer le recalcul du layout
                document.body.style.display = 'none';
                document.body.offsetHeight;
                document.body.style.display = '';
            })()
        """.trimIndent()

        webView.loadUrl(css)
    }

    fun loadUrl(url: String) {
        showProgressBar()
        hasAttemptedLogin = false
        webView.loadUrl(url)
    }

    fun goBack() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    fun canGoBack(): Boolean {
        return webView.canGoBack()
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    fun clearCache() {
        webView.clearCache(true)
        webView.clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
    }

    fun executeJavaScript(script: String) {
        webView.evaluateJavascript(script, null)
    }
}