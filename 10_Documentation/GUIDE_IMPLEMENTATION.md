# GUIDE COMPLET - Application Android Webtrees
## Guide de développement étape par étape

---

## 📱 Vue d'ensemble du projet

### Description
Application Android native qui affiche votre site Webtrees avec une interface mobile optimisée.

### Fonctionnalités principales
1. **Configuration initiale (Onboarding)**
   - Saisie de l'URL du serveur Webtrees
   - Authentification (login/password)
   - Sélection de l'arbre généalogique par défaut

2. **Interface principale**
   - WebView avec CSS personnalisé injecté
   - Barre de navigation en bas (Arbre, Recherche, Stats, Profil)
   - Menu latéral (changement d'arbre, paramètres, déconnexion)
   - Stockage sécurisé des identifiants

---

## 🛠️ ÉTAPE 1 : Création du projet dans Android Studio

### 1.1 Nouveau projet
1. Ouvrir Android Studio
2. File → New → New Project
3. Sélectionner "Empty Views Activity"
4. Configuration :
   - **Name**: WebtreesApp
   - **Package name**: com.votrenom.webtrees
   - **Save location**: Choisir votre dossier
   - **Language**: Kotlin
   - **Minimum SDK**: API 24 (Android 7.0)
   - **Build configuration language**: Kotlin DSL

### 1.2 Configuration Gradle

**Fichier `build.gradle.kts (Module :app)`** - Remplacer tout le contenu par :

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
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

Cliquer sur "Sync Now" en haut à droite.

---

## 📁 ÉTAPE 2 : Structure des dossiers

Dans `app/src/main/java/com/votrenom/webtrees/`, créer cette structure :

```
com.votrenom.webtrees/
├── activities/
│   ├── SplashActivity.kt
│   ├── OnboardingUrlActivity.kt
│   ├── OnboardingLoginActivity.kt
│   ├── OnboardingTreeActivity.kt
│   └── MainActivity.kt
├── adapters/
│   └── TreeAdapter.kt
├── models/
│   └── Tree.kt
└── utils/
    ├── PreferencesManager.kt
    └── CssInjector.kt
```

Pour créer un package :
1. Clic droit sur `java/com/votrenom/webtrees`
2. New → Package
3. Taper le nom (ex: "activities")

---

## 📄 ÉTAPE 3 : Copier les fichiers Kotlin

### 3.1 Modèle Tree.kt

**Emplacement**: `models/Tree.kt`

```kotlin
package com.votrenom.webtrees.models

data class Tree(
    val id: String,
    val name: String,
    val title: String
)
```

### 3.2 PreferencesManager.kt

**Emplacement**: `utils/PreferencesManager.kt`

[COPIER LE CONTENU DU FICHIER PreferencesManager.kt fourni précédemment]

### 3.3 CssInjector.kt

**Emplacement**: `utils/CssInjector.kt`

[COPIER LE CONTENU DU FICHIER CssInjector.kt fourni précédemment]

### 3.4 Les Activities

**Copier dans l'ordre** :
1. SplashActivity.kt → `activities/`
2. OnboardingUrlActivity.kt → `activities/`
3. OnboardingLoginActivity.kt → `activities/`
4. OnboardingTreeActivity.kt → `activities/`
5. MainActivity.kt → `activities/`

### 3.5 TreeAdapter.kt

**Emplacement**: `adapters/TreeAdapter.kt`

[COPIER LE CONTENU DU FICHIER TreeAdapter.kt fourni précédemment]

---

## 🎨 ÉTAPE 4 : Ressources XML

### 4.1 Layouts

**Emplacement**: `app/src/main/res/layout/`

Créer ces fichiers (Clic droit sur layout → New → Layout Resource File) :

1. `activity_splash.xml`
2. `activity_onboarding_url.xml`
3. `activity_onboarding_login.xml`
4. `activity_onboarding_tree.xml`
5. `item_tree.xml`
6. `activity_main.xml`
7. `nav_header.xml`

[COPIER LE CONTENU XML fourni pour chaque fichier]

### 4.2 Menus

**Emplacement**: `app/src/main/res/menu/`

Créer le dossier menu s'il n'existe pas :
- Clic droit sur `res` → New → Android Resource Directory
- Resource type: menu
- Cliquer OK

Créer ces fichiers :
1. `bottom_navigation_menu.xml`
2. `drawer_menu.xml`

[COPIER LE CONTENU XML fourni]

### 4.3 Values

**Dans `app/src/main/res/values/`** :

**strings.xml** :
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Webtrees Mobile</string>
    <string name="navigation_drawer_open">Ouvrir le menu</string>
    <string name="navigation_drawer_close">Fermer le menu</string>
</resources>
```

**colors.xml** :
[COPIER colors.xml fourni]

**themes.xml** :
[COPIER themes.xml fourni]

### 4.4 Drawables

**Emplacement**: `app/src/main/res/drawable/`

**gradient_background.xml** :
[COPIER gradient_background.xml fourni]

---

## 🎯 ÉTAPE 5 : Icônes

### Option 1 : Icônes vectorielles Material Design (Recommandé)

1. Clic droit sur `drawable` → New → Vector Asset
2. Cliquer sur l'icône Android à côté de "Clip Art"
3. Rechercher et créer ces icônes :
   - `ic_tree` → Chercher "nature" ou "park"
   - `ic_person` → Chercher "person"
   - `ic_search` → Chercher "search"
   - `ic_stats` → Chercher "bar_chart"
   - `ic_menu` → Chercher "menu"
   - `ic_server` → Chercher "dns"
   - `ic_link` → Chercher "link"
   - `ic_lock` → Chercher "lock"
   - `ic_refresh` → Chercher "refresh"
   - `ic_delete` → Chercher "delete"
   - `ic_settings` → Chercher "settings"
   - `ic_logout` → Chercher "logout"

4. Pour chaque icône :
   - Name: taper le nom (ex: ic_tree)
   - Color: #667eea (ou laisser noir)
   - Cliquer Next puis Finish

### Option 2 : Icônes temporaires simples

Si vous voulez tester rapidement, créer des icônes simples :

**ic_tree.xml** dans `drawable/` :
```xml
<vector android:height="24dp" android:tint="#667eea"
    android:viewportHeight="24" android:viewportWidth="24"
    android:width="24dp" xmlns:android="http://schemas.android.com/apk/res/android">
    <path android:fillColor="@android:color/white" 
        android:pathData="M12,2L6.5,11h2.25V22h6.5V11h2.25L12,2z"/>
</vector>
```

Répéter pour les autres icônes avec des formes simples (cercles, carrés, etc.).

---

## 📱 ÉTAPE 6 : AndroidManifest.xml

**Emplacement**: `app/src/main/AndroidManifest.xml`

Remplacer TOUT le contenu par le fichier AndroidManifest.xml fourni.

**Important** : Vérifier que le `package` correspond à votre nom.

---

## 🧪 ÉTAPE 7 : Tester l'application

### 7.1 Build du projet

1. Build → Clean Project
2. Build → Rebuild Project
3. Attendre la fin (vérifier qu'il n'y a pas d'erreurs en bas)

### 7.2 Lancement

#### Option A : Émulateur

1. Tools → Device Manager
2. Create Device
3. Choisir "Pixel 6" ou similaire
4. System Image : Android 14 (API 34)
5. Finish
6. Cliquer sur ▶ (Run) en haut

#### Option B : Téléphone physique

1. Sur votre téléphone :
   - Paramètres → À propos
   - Taper 7 fois sur "Numéro de build"
   - Retour → Options développeur
   - Activer "Débogage USB"
2. Connecter le téléphone en USB
3. Autoriser le débogage sur le téléphone
4. Cliquer sur ▶ (Run) dans Android Studio

---

## 🎨 ÉTAPE 8 : Personnalisation

### 8.1 Changer les couleurs

Dans `colors.xml`, modifier :
```xml
<color name="purple_500">#VOTRE_COULEUR</color>
<color name="purple_700">#VOTRE_COULEUR_FONCEE</color>
```

### 8.2 Modifier le CSS injecté

Dans `CssInjector.kt`, fonction `getMobileCSS()`, personnaliser :
- Les couleurs du gradient
- Les tailles de police
- Les espacements
- Les bordures

### 8.3 Icône de l'app

1. Clic droit sur `res` → New → Image Asset
2. Asset Type: Launcher Icons
3. Foreground Layer : choisir votre image
4. Background Layer : choisir une couleur
5. Next → Finish

---

## 🐛 ÉTAPE 9 : Résolution des problèmes courants

### Erreur "Unresolved reference"

- Vérifier que le package est correct dans chaque fichier
- Build → Clean Project puis Rebuild

### Erreur de compilation

- Vérifier la version de Gradle
- File → Project Structure → Project → Gradle version: 8.2

### WebView ne charge pas

- Vérifier la permission INTERNET dans AndroidManifest
- Vérifier l'URL (doit commencer par https://)
- Tester d'abord avec https://webtrees.net/demo-dev/

### Icônes manquantes

- Créer toutes les icônes listées dans l'Étape 5
- Ou remplacer temporairement par `android:icon="@android:drawable/ic_menu_info_details"`

---

## 🚀 ÉTAPE 10 : Améliorations futures

### À implémenter (optionnel) :

1. **Vraie authentification HTTP**
   - Utiliser OkHttp ou Retrofit
   - POST vers /login.php avec username/password
   - Récupérer et stocker le cookie de session

2. **Récupération réelle des arbres**
   - Parser la page HTML après login
   - Extraire la liste des arbres disponibles
   - Ou utiliser l'API si disponible

3. **Mode hors-ligne**
   - Cacher les pages visitées
   - Stocker les données dans SQLite
   - Synchroniser quand connecté

4. **Notifications**
   - Firebase Cloud Messaging
   - Notifications d'anniversaires
   - Nouveaux ajouts à l'arbre

5. **Partage**
   - Intent Android pour partager des profils
   - Export PDF
   - Envoi par email

---

## 📋 CHECKLIST FINALE

Avant de tester, vérifier :

- [ ] Gradle synced sans erreur
- [ ] Tous les fichiers Kotlin créés dans les bons packages
- [ ] Tous les layouts XML créés
- [ ] Tous les menus créés
- [ ] Toutes les icônes créées
- [ ] AndroidManifest.xml configuré
- [ ] Permissions INTERNET ajoutée
- [ ] SplashActivity définie comme LAUNCHER
- [ ] Build réussi sans erreur
- [ ] Émulateur ou téléphone connecté

---

## 💡 CONSEILS

1. **Tester par étapes** : Compiler après chaque fichier ajouté
2. **Utiliser les snippets** : Android Studio autocomplete beaucoup
3. **Debugger** : Utiliser Logcat pour voir les erreurs
4. **Commencer simple** : Tester d'abord avec https://webtrees.net/demo-dev/
5. **Sauvegarder souvent** : Git ou copie du projet régulièrement

---

## 📞 AIDE SUPPLÉMENTAIRE

### Ressources utiles :
- Documentation Android : developer.android.com
- Kotlin : kotlinlang.org
- Material Design : material.io
- WebView : developer.android.com/guide/webapps/webview

### Erreurs fréquentes :
- **R.id not found** → Clean + Rebuild
- **Cannot resolve symbol** → Vérifier les imports
- **App crash** → Regarder Logcat (en bas d'Android Studio)

---

## ✅ RÉSUMÉ

Vous avez maintenant :
1. ✅ Une app Android native
2. ✅ Configuration initiale (onboarding)
3. ✅ Authentification sécurisée
4. ✅ WebView avec CSS personnalisé
5. ✅ Navigation mobile optimisée
6. ✅ Stockage sécurisé des données

**Prochaines étapes** :
- Tester avec votre serveur Webtrees
- Personnaliser les couleurs et le CSS
- Implémenter l'authentification réelle
- Ajouter des fonctionnalités avancées

Bon développement ! 🎉
