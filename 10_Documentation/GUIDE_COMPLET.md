# Guide Complet - Application Webtrees pour Android

## 📋 Vue d'ensemble du projet

### Fonctionnalités principales
1. **Configuration initiale** (première ouverture uniquement)
   - Page 1 : Saisie de l'URL du site Webtrees
   - Page 2 : Login et mot de passe
   - Page 3 : Choix de l'arbre généalogique par défaut

2. **Application principale**
   - Affichage du site Webtrees avec CSS personnalisé
   - Barre de navigation en bas (Arbre, Recherche, Stats, Profil)
   - Menu hamburger pour changer d'arbre ou se déconnecter
   - Mode hors-ligne basique avec cache

3. **Fonctionnalités avancées**
   - Sauvegarde sécurisée des identifiants
   - Navigation fluide dans l'app
   - Injection CSS automatique
   - Gestion du retour arrière

## 🏗️ Architecture du projet

```
WebtreesApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/votrenom/webtrees/
│   │   │   │   ├── activities/
│   │   │   │   │   ├── SplashActivity.kt
│   │   │   │   │   ├── OnboardingUrlActivity.kt
│   │   │   │   │   ├── OnboardingLoginActivity.kt
│   │   │   │   │   ├── OnboardingTreeActivity.kt
│   │   │   │   │   └── MainActivity.kt
│   │   │   │   ├── utils/
│   │   │   │   │   ├── PreferencesManager.kt
│   │   │   │   │   └── CssInjector.kt
│   │   │   │   └── models/
│   │   │   │       └── Tree.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   └── drawable/
│   │   │   └── AndroidManifest.xml
```

## 🚀 Étape 1 : Création du projet dans Android Studio

### 1.1 Créer un nouveau projet
- Ouvrir Android Studio
- "New Project" → "Empty Activity"
- **Name**: WebtreesApp
- **Package name**: com.votrenom.webtrees (remplacez par votre nom)
- **Language**: Kotlin
- **Minimum SDK**: API 24 (Android 7.0) - couvre 95% des appareils

### 1.2 Configuration Gradle

Fichier `build.gradle.kts (Module: app)`:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.votrenom.webtrees"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.votrenom.webtrees"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    buildFeatures {
        viewBinding = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Pour les préférences sécurisées
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Pour ViewPager2 (onboarding)
    implementation("androidx.viewpager2:viewpager2:1.0.0")
}
```

## 📱 Étape 2 : Créer les modèles de données

### 2.1 Tree.kt
```kotlin
package com.votrenom.webtrees.models

data class Tree(
    val id: String,
    val name: String,
    val title: String
)
```

## 💾 Étape 3 : Gestionnaire de préférences sécurisé

### 3.1 PreferencesManager.kt
```kotlin
package com.votrenom.webtrees.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferencesManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "webtrees_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_DEFAULT_TREE = "default_tree"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_COOKIE = "session_cookie"
    }
    
    // URL du serveur
    fun saveUrl(url: String) {
        securePrefs.edit().putString(KEY_URL, url).apply()
    }
    
    fun getUrl(): String? {
        return securePrefs.getString(KEY_URL, null)
    }
    
    // Identifiants
    fun saveCredentials(username: String, password: String) {
        securePrefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, password)
            .apply()
    }
    
    fun getUsername(): String? {
        return securePrefs.getString(KEY_USERNAME, null)
    }
    
    fun getPassword(): String? {
        return securePrefs.getString(KEY_PASSWORD, null)
    }
    
    // Arbre par défaut
    fun saveDefaultTree(treeId: String, treeName: String) {
        securePrefs.edit()
            .putString(KEY_DEFAULT_TREE, "$treeId|$treeName")
            .apply()
    }
    
    fun getDefaultTree(): Pair<String, String>? {
        val saved = securePrefs.getString(KEY_DEFAULT_TREE, null) ?: return null
        val parts = saved.split("|")
        return if (parts.size == 2) Pair(parts[0], parts[1]) else null
    }
    
    // Cookie de session
    fun saveCookie(cookie: String) {
        securePrefs.edit().putString(KEY_COOKIE, cookie).apply()
    }
    
    fun getCookie(): String? {
        return securePrefs.getString(KEY_COOKIE, null)
    }
    
    // Onboarding
    fun setOnboardingCompleted(completed: Boolean) {
        securePrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
    
    fun isOnboardingCompleted(): Boolean {
        return securePrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    // Déconnexion
    fun clearAll() {
        securePrefs.edit().clear().apply()
    }
}
```

## 🎨 Étape 4 : Injecteur CSS personnalisé

### 4.1 CssInjector.kt
```kotlin
package com.votrenom.webtrees.utils

object CssInjector {
    
    fun getMobileCSS(): String {
        return """
            /* Cache les éléments desktop */
            .wt-header-wrapper,
            .wt-footer,
            .desktop-only {
                display: none !important;
            }
            
            /* Optimise le body */
            body {
                margin: 0 !important;
                padding: 0 !important;
                padding-bottom: 70px !important;
                background: #f5f5f5 !important;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif !important;
            }
            
            /* Style des cartes */
            .wt-main-container {
                padding: 0 !important;
            }
            
            /* Profil individu - style carte */
            .wt-page-content {
                background: white !important;
                border-radius: 0 !important;
                box-shadow: none !important;
                margin: 0 !important;
            }
            
            /* Headers */
            h1, h2, h3 {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
                color: white !important;
                padding: 15px 20px !important;
                margin: 0 0 1px 0 !important;
                font-size: 18px !important;
            }
            
            /* Tables et facts */
            .wt-page-content table {
                width: 100% !important;
                border: none !important;
                background: white !important;
            }
            
            .wt-page-content table tr {
                border-bottom: 1px solid #f0f0f0 !important;
            }
            
            .wt-page-content table td {
                padding: 15px 20px !important;
                border: none !important;
            }
            
            .wt-page-content table th {
                background: transparent !important;
                color: #666 !important;
                font-weight: normal !important;
                font-size: 14px !important;
                padding: 15px 20px !important;
                text-align: left !important;
            }
            
            /* Liens */
            a {
                color: #667eea !important;
                text-decoration: none !important;
            }
            
            /* Boutons */
            .btn, button, input[type="submit"] {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
                color: white !important;
                border: none !important;
                padding: 12px 24px !important;
                border-radius: 8px !important;
                font-size: 16px !important;
                min-height: 48px !important;
            }
            
            /* Formulaires */
            input[type="text"],
            input[type="search"],
            select {
                width: 100% !important;
                padding: 12px !important;
                border: 1px solid #ddd !important;
                border-radius: 8px !important;
                font-size: 16px !important;
                margin: 5px 0 !important;
            }
            
            /* Menu et navigation - caché car on utilise la barre native */
            .wt-primary-navigation,
            .wt-secondary-navigation {
                display: none !important;
            }
            
            /* Images et avatars */
            img {
                max-width: 100% !important;
                height: auto !important;
                border-radius: 8px !important;
            }
            
            /* Liste des personnes */
            .wt-individual-list-item {
                background: white !important;
                padding: 15px !important;
                margin-bottom: 1px !important;
                display: flex !important;
                align-items: center !important;
            }
            
            /* Espacement mobile-friendly */
            * {
                -webkit-tap-highlight-color: rgba(0,0,0,0.1);
            }
        """.trimIndent()
    }
    
    fun getJavaScriptInjection(): String {
        return """
            (function() {
                var style = document.createElement('style');
                style.innerHTML = `${getMobileCSS()}`;
                document.head.appendChild(style);
            })();
        """.trimIndent()
    }
}
```

## 📄 Étape 5 : Layouts XML

Je vais créer tous les layouts dans les prochains messages. Voulez-vous que je continue avec :
1. Les layouts XML pour chaque écran ?
2. Les Activities Kotlin ?
3. Les configurations AndroidManifest ?

Dites-moi par où vous voulez commencer !
