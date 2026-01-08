package com.genealogie.webtrees

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("WebtreesPrefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_SITE_URL = "site_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_DEFAULT_TREE = "default_tree"
        private const val KEY_SETUP_COMPLETED = "setup_completed"
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
    
    // Effacer toutes les données (pour se déconnecter)
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
