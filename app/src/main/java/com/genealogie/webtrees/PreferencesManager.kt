package com.genealogie.webtrees

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("WebtreesPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SITE_URL = "site_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_DEFAULT_TREE = "default_tree"
        private const val KEY_SETUP_COMPLETED = "setup_completed"
        private const val KEY_OAUTH_CLIENT_ID = "oauth_client_id"
        private const val KEY_OAUTH_CLIENT_SECRET = "oauth_client_secret"
        private const val KEY_CACHED_TREES = "cached_trees"
    }
    
    // Sauvegarder l'URL du site
    fun saveSiteUrl(url: String) {
        prefs.edit().putString(KEY_SITE_URL, url).apply()
    }
    
    fun getSiteUrl(): String? {
        return prefs.getString(KEY_SITE_URL, null)
    }
    
    // Sauvegarder les identifiants
    fun saveCredentials(username: String, password: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }
    
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }
    
    fun getPassword(): String? {
        return prefs.getString(KEY_PASSWORD, null)
    }
    
    // Sauvegarder l'arbre par défaut
    fun saveDefaultTree(treeName: String) {
        prefs.edit().putString(KEY_DEFAULT_TREE, treeName).apply()
    }
    
    fun getDefaultTree(): String? {
        return prefs.getString(KEY_DEFAULT_TREE, null)
    }
    
    // Marquer la configuration comme terminée
    fun setSetupCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETED, completed).apply()
    }
    
    fun isSetupCompleted(): Boolean {
        return prefs.getBoolean(KEY_SETUP_COMPLETED, false)
    }
    
    fun saveOAuthCredentials(clientId: String, clientSecret: String) {
        prefs.edit()
            .putString(KEY_OAUTH_CLIENT_ID, clientId)
            .putString(KEY_OAUTH_CLIENT_SECRET, clientSecret)
            .apply()
    }

    fun getOAuthClientId(): String? = prefs.getString(KEY_OAUTH_CLIENT_ID, null)
    fun getOAuthClientSecret(): String? = prefs.getString(KEY_OAUTH_CLIENT_SECRET, null)
    fun hasOAuthCredentials() = !getOAuthClientId().isNullOrEmpty() && !getOAuthClientSecret().isNullOrEmpty()

    fun saveCachedTrees(trees: List<Tree>) {
        val arr = JSONArray()
        trees.forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("title", t.title)
            })
        }
        prefs.edit().putString(KEY_CACHED_TREES, arr.toString()).apply()
    }

    fun getCachedTrees(): List<Tree> {
        val str = prefs.getString(KEY_CACHED_TREES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(str)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                Tree(obj.getInt("id"), obj.getString("name"), obj.getString("title"))
            }
        } catch (e: Exception) { emptyList() }
    }

    // Effacer toutes les données (pour se déconnecter)
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
