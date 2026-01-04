package com.votredomaine.webtrees

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.CookieManager
import android.view.View
import android.widget.ProgressBar

class WebViewManager(
    private val webView: WebView,
    private val progressBar: ProgressBar
) {

    fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectCustomCSS()
                hideProgressBar()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
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

    private fun injectCustomCSS() {
        val css = """
            javascript:(function() {
                var style = document.createElement('style');
                style.innerHTML = `
                    body {
                        margin: 0 !important;
                        padding: 0 !important;
                        background: #f5f5f5 !important;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
                    }
                    
                    .wt-header-wrapper, .wt-footer-container, #wt-footer {
                        display: none !important;
                    }
                    
                    .wt-main-container, .wt-main, main {
                        padding: 0 !important;
                        margin: 0 !important;
                        max-width: 100% !important;
                    }
                    
                    .wt-facts-table, .wt-page-content, .card {
                        background: white !important;
                        border-radius: 0 !important;
                        box-shadow: none !important;
                        margin: 1px 0 !important;
                        padding: 20px !important;
                        border: none !important;
                    }
                    
                    h1, h2, h3 {
                        color: #333 !important;
                        font-weight: 600 !important;
                    }
                    
                    h2 {
                        font-size: 22px !important;
                        margin-bottom: 15px !important;
                        color: #667eea !important;
                    }
                    
                    table {
                        width: 100% !important;
                        border-collapse: collapse !important;
                    }
                    
                    table tr {
                        border-bottom: 1px solid #f0f0f0 !important;
                    }
                    
                    table td, table th {
                        padding: 12px 0 !important;
                        border: none !important;
                        font-size: 14px !important;
                    }
                    
                    table td:first-child, table th:first-child {
                        color: #666 !important;
                        font-weight: normal !important;
                        width: 40% !important;
                    }
                    
                    table td:last-child {
                        color: #333 !important;
                        font-weight: 500 !important;
                        text-align: right !important;
                    }
                    
                    a {
                        color: #667eea !important;
                        text-decoration: none !important;
                    }
                    
                    .btn, button, input[type="submit"] {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
                        color: white !important;
                        border: none !important;
                        padding: 12px 24px !important;
                        border-radius: 12px !important;
                        font-size: 16px !important;
                        font-weight: 500 !important;
                    }
                    
                    input[type="text"], input[type="search"], input[type="email"], select, textarea {
                        width: 100% !important;
                        padding: 12px !important;
                        border: 1px solid #e0e0e0 !important;
                        border-radius: 8px !important;
                        font-size: 16px !important;
                        margin: 8px 0 !important;
                    }
                    
                    img {
                        max-width: 100% !important;
                        height: auto !important;
                        border-radius: 8px !important;
                    }
                    
                    .wt-sidebar {
                        display: none !important;
                    }
                    
                    .wt-chart-box {
                        background: white !important;
                        border: 2px solid #667eea !important;
                        border-radius: 12px !important;
                        padding: 10px !important;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1) !important;
                    }
                    
                    .wt-individual-list-item, .person-box {
                        background: white !important;
                        border-radius: 12px !important;
                        padding: 15px !important;
                        margin: 8px 0 !important;
                        box-shadow: 0 1px 3px rgba(0,0,0,0.1) !important;
                        border-left: 4px solid #667eea !important;
                    }
                    
                    .badge {
                        background: #667eea !important;
                        color: white !important;
                        padding: 4px 12px !important;
                        border-radius: 12px !important;
                        font-size: 12px !important;
                    }
                    
                    * {
                        -webkit-tap-highlight-color: rgba(102, 126, 234, 0.1) !important;
                    }
                    
                    .breadcrumb {
                        display: none !important;
                    }
                `;
                document.head.appendChild(style);
            })()
        """.trimIndent()

        webView.loadUrl(css)
    }

    fun loadUrl(url: String) {
        showProgressBar()
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
}
